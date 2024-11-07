buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.oss.licenses.plugin)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ktlint.gradle) apply false
}