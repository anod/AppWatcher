allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0-alpha11")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath(libs.kotlin.gradle.plugin)
    }
}