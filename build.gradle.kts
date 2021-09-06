buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://maven.fabric.io/public")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath(kotlin("gradle-plugin", version = "1.5.21"))
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}