buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.oss.licenses.plugin)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin) apply false
}