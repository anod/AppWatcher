dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.20")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")

            version("coroutines", "1.6.4")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")
        }
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
        ":lib:permissions"
)