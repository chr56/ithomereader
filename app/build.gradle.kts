import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidGradlePlugin)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

fun Project.getGitHash(shortHash: Boolean): String =
    ByteArrayOutputStream().use { stdout ->
        exec {
            if (shortHash) {
                commandLine("git", "rev-parse", "--short", "HEAD")
            } else {
                commandLine("git", "rev-parse", "HEAD")
            }
            standardOutput = stdout
        }
        stdout
    }.toString().trim()


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
    compileSdk = 34
    buildToolsVersion = "34.0.0"

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

        versionCode = 190
        versionName = "5.1.0-dev1"

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
    implementation(libs.retrofit2.retrofit)
    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.retrofit2.moshi)
    // debugImplementation(libs.leakcanary.android)
    // releaseImplementation(libs.leakcanary.androidNoOp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}