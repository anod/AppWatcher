import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("android")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://maven.google.com")
    google()
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            matchingFallbacks = listOf("", "")
            postprocessing {
                isRemoveUnusedCode = false
                isRemoveUnusedResources = false
                isObfuscate = false
                isOptimizeCode = false
                proguardFile("proguard-rules.pro")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(files("libs/keyczar-0.71g-090613.jar"))
    implementation("androidx.collection:collection:1.1.0")
    implementation("androidx.collection:collection-ktx:1.1.0")
    api(group = "com.google.protobuf", name = "protobuf-javalite", version = "3.11.4")
    api("com.android.volley:volley:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.5.0")

    implementation(project(":framework"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
}
