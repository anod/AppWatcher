plugins {
    id("com.android.library")
    kotlin("android")
}

repositories {
    jcenter()
    maven(url = "https://maven.google.com")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 200
        versionName = "2.0.0"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha06")
    implementation("com.google.android.material:material:1.3.0-alpha03")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.window:window:1.0.0-alpha01")
    implementation("androidx.core:core-ktx:1.3.2")

    implementation("com.google.android.gms:play-services-identity:17.0.0")
    implementation("com.google.android.gms:play-services-auth:18.1.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
}