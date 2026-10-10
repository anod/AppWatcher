import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
}

kotlin {
    androidLibrary {
        namespace = "info.anodsplace.playstore"
        compileSdk = 37
        minSdk = 31
        androidResources {
            enable = true
        }
        withJava()
        withHostTest {
            isIncludeAndroidResources = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain {
            kotlin.srcDir("src/androidMain/java")
            dependencies {
                implementation(files("libs/keyczar-0.71g-090613.jar"))
                api("com.google.protobuf:protobuf-javalite:4.36.2")
                implementation(libs.okhttp)

                implementation(project(":lib:applog"))
                implementation(project(":lib:framework"))
                implementation(libs.coroutines.core)
                implementation(libs.coroutines.android)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.test.core)
                implementation(libs.robolectric)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}