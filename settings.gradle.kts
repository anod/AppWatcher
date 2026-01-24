dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13647192/artifacts/repository")
        }
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13647192/artifacts/repository")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    ":app",
    ":playstore",
    ":lib:applog",
    ":lib:compose",
    ":lib:graphics",
    ":lib:context",
    ":lib:notification",
    ":lib:framework",
    ":lib:ktx",
    ":lib:permissions",
    ":lib:playservices",
    ":baselineprofile",
)