plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp").version("1.8.20-1.0.11")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdk = 27
        targetSdk = 33
        versionCode = 16102
        versionName = "1.6.1"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            extra["enableCrashlytics"] = false
        }

        create("release") {
            storeFile = file(findProperty("APPWATCHER_KEYSTORE_FILE") ?: ".")
            storePassword = findProperty("APPWATCHER_KEYSTORE_PASSWORD") as? String
            keyAlias = findProperty("APPWATCHER_KEY_ALIAS") as? String
            keyPassword = findProperty("APPWATCHER_KEY_PASSWORD") as? String
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-project.txt")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-project.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            proguardFiles("benchmark-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/license*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/notice*"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    namespace = "com.anod.appwatcher"
}

// https://youtrack.jetbrains.com/issue/KT-55947
kotlin {
    jvmToolchain(17)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "11"
}

dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1") // AppCompatActivity
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.core:core:1.10.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha04")
    implementation("androidx.paging:paging-compose:1.0.0-alpha18")

    // Compose
    implementation("androidx.activity:activity-compose:1.7.1")
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-alpha03")
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.28.0")

    // Coil
    implementation("io.coil-kt:coil-base:2.3.0")
    implementation("io.coil-kt:coil-compose-base:2.3.0")

    // Google
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")
    implementation("com.google.android.gms:play-services-identity:18.0.1")
    implementation("com.google.android.gms:play-services-auth:20.4.1")

    implementation("com.google.api-client:google-api-client:1.33.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20211107-1.32.1") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "commons-logging")
    }

    implementation("com.google.firebase:firebase-analytics:21.2.2")
    implementation("com.google.firebase:firebase-crashlytics:18.3.6")

    // 3rd party
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.jakewharton:process-phoenix:2.1.2")
    implementation("io.insert-koin:koin-core:3.4.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")

    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    ksp("androidx.room:room-compiler:2.5.1")

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.kotlin.stdlib)

    testImplementation("junit:junit:4.13.2")

    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:ktx"))
    implementation(project(":lib:permissions"))
    implementation(project(":playstore"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")