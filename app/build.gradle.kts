plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp").version("1.7.10-1.0.6")
    id("com.google.android.gms.oss-licenses-plugin")
}

repositories {
    mavenCentral()
    google()
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdk = 27
        targetSdk = 32
        versionCode = 15003
        versionName = "1.5.0"
        resValue("string", "content_authority", "com.anod.appwatcher")

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.incremental", "true")
            }
        }
    }

    buildFeatures {
        viewBinding = true
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
            proguardFile("../proguard-project.txt")
            resValue("string", "content_authority", "com.anod.appwatcher.debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("../proguard-project.txt")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        disable.addAll(listOf("InvalidPackage", "MissingTranslation"))
        informational.add("DuplicatePlatformClasses")
        checkReleaseBuilds = true
        abortOnError = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packagingOptions {
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0-rc01"
    }
    namespace = "com.anod.appwatcher"
}

dependencies {
    // AndroidX
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime:2.7.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.core:core:1.8.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.5.1")

    implementation("androidx.paging:paging-runtime:3.2.0-alpha01")
    implementation("androidx.paging:paging-compose:1.0.0-alpha15")

    // Compose
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.compose.ui:ui-viewbinding:1.2.0")
    implementation("com.google.accompanist:accompanist-pager:0.25.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.25.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.25.0")
    implementation("io.coil-kt:coil-compose-base:2.1.0")

    // Google
    implementation("com.google.android.material:material:1.6.1")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.android.gms:play-services-identity:18.0.1")
    implementation("com.google.android.gms:play-services-auth:20.2.0")

    implementation("com.google.api-client:google-api-client:1.33.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20211107-1.32.1") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "commons-logging")
    }

    implementation("com.google.firebase:firebase-analytics:21.1.0")
    implementation("com.google.firebase:firebase-crashlytics:18.2.12")

    // 3rd party
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("io.coil-kt:coil-base:2.1.0")
    implementation("com.jakewharton:process-phoenix:2.1.2")
    implementation("io.insert-koin:koin-core:3.2.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.5.1")

    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
    ksp("androidx.room:room-compiler:2.4.3")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")

    testImplementation("junit:junit:4.13.2")

    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:colorpicker"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:ktx"))
    implementation(project(":playstore"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")