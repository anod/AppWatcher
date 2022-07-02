buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}