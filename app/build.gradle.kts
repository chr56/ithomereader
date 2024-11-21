import tools.release.git.getGitHash
import tools.release.registerPublishTask
import java.util.Properties

plugins {
    alias(libs.plugins.androidGradlePlugin)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.artifactsRelease)
}

val signingConfigFile: File = rootProject.file("signing.properties")
var signingProperties = Properties()
if (signingConfigFile.exists()) {
    signingConfigFile.inputStream().use { signingProperties.load(it) }
}

val secretsConfigFile: File = rootProject.file("secrets.properties")
val secretsProperties: Properties = Properties()
if (secretsConfigFile.exists()) {
    secretsConfigFile.inputStream().use { secretsProperties.load(it) }
}

android {
    namespace = "me.ikirby.ithomereader"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    signingConfigs {
        if (signingConfigFile.exists()) {
            create("release") {
                keyAlias = signingProperties["keyAlias"] as String
                keyPassword = signingProperties["keyPassword"] as String
                storeFile = file(signingProperties["storeFile"] as String)
                storePassword = signingProperties["storePassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "me.ikirby.ithomereader"

        minSdk = 23
        targetSdk = 34

        versionCode = 193
        versionName = "5.1.1-dev2"

        resourceConfigurations += setOf("zh-rCN")
        versionNameSuffix = "-" + getGitHash(true)
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        named("debug") {
            applicationIdSuffix = ".debug"
        }
        create("RC") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            applicationIdSuffix = ".rc"
            versionNameSuffix = "-rc"
        }
    }
    flavorDimensions += listOf("update")
    productFlavors {
        create("nornal") {
            dimension = "update"
            manifestPlaceholders += mapOf("updateUrl" to secretsProperties["updateUrl"] as String)
        }
        create("noupdate") {
            dimension = "update"
            versionNameSuffix = "-pub"
            manifestPlaceholders += mapOf("updateUrl" to "")
        }
    }
    packaging {
        resources.pickFirsts.add("META-INF/atomicfu.kotlin_module")
        resources.excludes.add("META-INF/CHANGES")
        resources.excludes.add("META-INF/README.md")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/*.version")
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    androidComponents {

        val moduleName = project.name
        onVariants(selector().all()) { variant ->
            // Rename
            for (output in variant.outputs) {
                val outputImpl = output as? com.android.build.api.variant.impl.VariantOutputImpl ?: continue
                val origin = outputImpl.outputFileName.get()
                val new = origin.replace(moduleName, "ITHomeReader-${output.versionName.get()}")
                outputImpl.outputFileName.set(new)
            }
        }

        onVariants(selector().withBuildType("release")) { variant ->
            tasks.registerPublishTask("ITHomeReader", variant)
        }
    }
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.core)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.jsoup)
    implementation(libs.photoView)
    implementation(libs.coil)

    implementation(libs.okhttp3)
    implementation(libs.retrofit2)
    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.retrofit2.moshi)
    // debugImplementation(libs.leakcanary.android)
    // releaseImplementation(libs.leakcanary.androidNoOp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.test.androidx.suite)
}