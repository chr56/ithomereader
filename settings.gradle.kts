@file:Suppress("UnstableApiUsage")

rootProject.buildFileName = "build.gradle.kts"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

include(":app")

