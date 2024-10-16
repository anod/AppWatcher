import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.anod.appwatcher.utils.Parcelize"
            )
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            // AndroidX
            implementation(libs.androidx.appcompat) // AppCompatActivity
            implementation(libs.androidx.palette)
            implementation(libs.work.runtime)

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.core.splashscreen)

            implementation(libs.material)

            implementation(libs.paging.compose.android)

            // Compose
            implementation(libs.androidx.activity.compose)
            implementation(libs.runtime.tracing)

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
            implementation(libs.process.phoenix)
            implementation(libs.ktor.client.okhttp)

            // ViewModel and LiveData
            implementation(libs.lifecycle.viewmodel.ktx)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.common.java8)

            //implementation("org.jetbrains:annotations:23.0.0")

            implementation(libs.coroutines.android)

            implementation(project(":lib:compose"))
            implementation(project(":lib:graphics"))
            implementation(project(":lib:context"))
            implementation(project(":lib:notification"))
            implementation(project(":lib:framework"))
            implementation(project(":lib:playservices"))
            implementation(project(":lib:permissions"))
            implementation(project(":playstore"))
        }
        commonMain.dependencies {
            implementation(libs.koin.core)

            implementation(libs.coil.core)
            implementation(libs.coil.compose.core)
            implementation(libs.coil.network.ktor3)

            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.sqlite.bundled)
            implementation(libs.paging.common)

            implementation(libs.coroutines.core)

            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(project(":lib:applog"))
            implementation(project(":lib:ktx"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdk = 27
        targetSdk = 34
        versionCode = 16602
        versionName = "1.6.6"
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

    namespace = "com.anod.appwatcher"
}


// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")