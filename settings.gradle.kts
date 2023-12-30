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
    ":lib:context",
    ":lib:notification",
    ":lib:framework",
    ":lib:ktx",
    ":lib:permissions",
    ":lib:playservices",
    ":macrobenchmark"
)