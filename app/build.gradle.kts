plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.oss.licenses.plugin")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1")
    google()
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 14700
        versionName = "1.4.7"
        resValue("string", "content_authority", "com.anod.appwatcher")

        vectorDrawables.generatedDensities("hdpi", "xxhdpi")

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
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFile(getDefaultProguardFile("proguard-android.txt"))

            resValue("string", "content_authority", "com.anod.appwatcher.debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("../proguard-project.txt")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lintOptions {
        disable("InvalidPackage", "MissingTranslation")
        informational("DuplicatePlatformClasses")
        isCheckReleaseBuilds = true
        // Or, if you prefer, you can continue to check for errors in close builds,
        // but continue the build even when errors are found:
        isAbortOnError = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
}

dependencies {
    // AndroidX
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.work:work-runtime:2.4.0")
    implementation("androidx.core:core:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.paging:paging-runtime:3.0.0-alpha10")
    // Google
    implementation("com.google.android.material:material:1.3.0-alpha04")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.android.gms:play-services-identity:17.0.0")
    implementation("com.google.android.gms:play-services-auth:18.1.0")

    implementation("com.google.apis:google-api-services-drive:v3-rev20200706-1.30.10") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "commons-logging")
    }

    implementation("com.google.api-client:google-api-client:1.30.10")
    implementation("com.google.api-client:google-api-client-android:1.30.10")

    implementation("com.google.firebase:firebase-analytics:18.0.0")
    implementation("com.google.firebase:firebase-crashlytics:17.3.0")

    // 3rd party
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.jakewharton:process-phoenix:2.0.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
    kapt("androidx.lifecycle:lifecycle-compiler:2.2.0")

    implementation("androidx.room:room-runtime:2.2.5")
    implementation("androidx.room:room-ktx:2.2.5")
    kapt("androidx.room:room-compiler:2.2.5")

    // KTX
    implementation("androidx.work:work-runtime-ktx:2.4.0")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.work:work-runtime-ktx:2.4.0")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.room:room-ktx:2.2.5")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.20")

    testImplementation("junit:junit:4.13.1")

    implementation(project(":lib:colorpicker"))
    implementation(project(":lib:framework"))
    implementation(project(":playstore"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.5")
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")