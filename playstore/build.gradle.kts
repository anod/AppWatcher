plugins {
    id("com.android.library")
    kotlin("android")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://maven.google.com")
    google()
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "info.anodsplace.playstore"
}

dependencies {
    implementation(files("libs/keyczar-0.71g-090613.jar"))
    implementation("androidx.collection:collection:1.2.0")
    implementation("androidx.collection:collection-ktx:1.2.0")
    // Update from 3.11.4 breaks parsing, may be needs to be regenerated
    api(group = "com.google.protobuf", name = "protobuf-javalite", version = "3.11.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation(project(":lib:applog"))
    implementation(project(":lib:framework"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.20")
}