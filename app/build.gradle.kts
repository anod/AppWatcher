import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.google.gms.oss.licenses.plugin")
    id("io.fabric")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    google()
}

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 140
        versionName = "1.4"
        resValue("string", "content_authority", "com.anod.appwatcher")

        vectorDrawables.generatedDensities("hdpi", "xxhdpi")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.incremental" to "true")
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            extra["enableCrashlytics"] = false
        }

        create("release") {
            storeFile = file(property("APPWATCHER_KEYSTORE_FILE")!!)
            storePassword = property("APPWATCHER_KEYSTORE_PASSWORD") as String
            keyAlias = property("APPWATCHER_KEY_ALIAS") as String
            keyPassword = property("APPWATCHER_KEY_PASSWORD") as String
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

    androidExtensions {
        isExperimental = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/license*")
        exclude("META-INF/NOTICE*")
        exclude("META-INF/notice*")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {
    // AndroidX
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta4")
    implementation("androidx.work:work-runtime:2.3.1")
    implementation("androidx.core:core:1.2.0-rc01")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")

    // Google
    implementation("com.google.android.material:material:1.2.0-alpha04")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.android.gms:play-services-identity:17.0.0")
    implementation("com.google.android.gms:play-services-auth:17.0.0")
    implementation("com.google.firebase:firebase-core:17.2.2")

    implementation("com.google.apis:google-api-services-drive:v3-rev20191108-1.30.3") {
        exclude(group = "org.apache.httpcomponents")
    }

    implementation("com.google.api-client:google-api-client:1.30.8")
    implementation("com.google.api-client:google-api-client-android:1.30.8")

    // 3rd party
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.3.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.jakewharton:process-phoenix:2.0.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    kapt("androidx.lifecycle:lifecycle-compiler:2.2.0")

    implementation("androidx.room:room-runtime:2.2.3")
    kapt("androidx.room:room-compiler:2.2.3")

    // KTX
    implementation("androidx.core:core-ktx:1.2.0-beta01")
    implementation("androidx.work:work-runtime-ktx:2.3.1")
    implementation("androidx.fragment:fragment-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.room:room-ktx:2.2.3")
    implementation("androidx.core:core-ktx:1.2.0-beta01")
    implementation("androidx.core:core-ktx:1.2.0-rc01")
    implementation("androidx.work:work-runtime-ktx:2.3.1")
    implementation("androidx.fragment:fragment-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.room:room-ktx:2.2.3")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    testImplementation("junit:junit:4.13")

    implementation(project(":colorpicker"))
    implementation(project(":framework"))
    implementation(project(":playstore"))
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
