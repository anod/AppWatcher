plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp").version("1.9.10-1.0.13")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdk = 27
        targetSdk = 33
        versionCode = 16301
        versionName = "1.6.3"
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
        kotlinCompilerExtensionVersion = "1.5.3"
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
    implementation(libs.androidx.appcompat) // AppCompatActivity
    implementation(libs.androidx.palette)
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // Compose
    implementation(libs.activity.compose)
    implementation(libs.runtime.tracing)
    implementation(libs.accompanist.swiperefresh)

    // Coil
    implementation(libs.coil.base)
    implementation(libs.coil.compose.base)

    // Google
    implementation(libs.play.services.oss.licenses)
    implementation(libs.play.services.identity)
    implementation(libs.play.services.auth)

    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    implementation("com.google.apis:google-api-services-drive:v3-rev20230520-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "commons-logging")
    }

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // 3rd party
    implementation(libs.okhttp)
    implementation(libs.process.phoenix)
    implementation(libs.koin.core)

    // ViewModel and LiveData
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.common.java8)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit)

    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:ktx"))
    implementation(project(":lib:permissions"))
    implementation(project(":playstore"))

    debugImplementation(libs.leakcanary.android)
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")