
buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://maven.fabric.io/public")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0-alpha09")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("io.fabric.tools:gradle:1.31.2")
        classpath(kotlin("gradle-plugin", version = "1.3.61"))
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")
    }
}