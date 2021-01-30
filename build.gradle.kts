buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://maven.fabric.io/public")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha05")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
        classpath(kotlin("gradle-plugin", version = "1.4.21"))
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}