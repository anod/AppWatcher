buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.oss.licenses.plugin)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ktlint.gradle) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.android.test) apply false
}

subprojects {
    tasks.withType<Test>().configureEach {
        // Robolectric 4.17 needs access to JDK internals on Java 17 and newer.
        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED"
        )
    }
}