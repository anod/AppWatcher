plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    compileSdk = 34

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
    // Update from 3.11.4 breaks parsing, may be needs to be regenerated
    api(group = "com.google.protobuf", name = "protobuf-javalite", version = "3.11.4")
    implementation(libs.okhttp)

    implementation(project(":lib:applog"))
    implementation(project(":lib:framework"))
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}