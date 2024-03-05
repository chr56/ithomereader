plugins {
    alias(libs.plugins.androidGradlePlugin) apply false
    alias(libs.plugins.kotlin.android) apply false
}
repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}