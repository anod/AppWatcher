buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://maven.fabric.io/public")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}