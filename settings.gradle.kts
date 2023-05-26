dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":app",
    ":playstore",
    ":lib:applog",
    ":lib:compose",
    ":lib:graphics",
    ":lib:framework",
    ":lib:ktx",
    ":lib:permissions",
    ":macrobenchmark"
)
