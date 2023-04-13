buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.5")
    }
}

plugins {
    id("com.android.application") version "8.1.0-alpha11" apply false
    id("com.android.library") version "8.1.0-alpha11" apply false
    alias(libs.plugins.kotlin.plugin) apply false
}