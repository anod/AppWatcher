import com.google.gms.googleservices.GoogleServicesTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ktlint.gradle)
    id("kotlin-parcelize")
    id("com.google.android.gms.oss-licenses-plugin")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

val composeMaterial3Version = libs.versions.compose.material3.get()

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "com.anod.appwatcher"
        minSdk = 31
        targetSdk = 36
        versionCode = 17102
        versionName = "1.7.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("proguard-project.txt")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
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
            excludes += "META-INF/INDEX.LIST"
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    namespace = "com.anod.appwatcher"
}

dependencies {
    constraints {
        // play-services-oss-licenses hard-requires an androidx.compose.material3 1.5.0 alpha for its
        // Compose licenses UI, and a Compose BOM constraint does not outrank a hard requirement. Keep
        // the catalog pin strict so a future transitive bump fails dependency resolution instead of
        // silently shipping a material3 that is ABI-skewed against the BOM's compose-foundation.
        implementation("androidx.compose.material3:material3") {
            version { strictly(composeMaterial3Version) }
            because("material3 must stay aligned with the compose-foundation version from the Compose BOM")
        }
        implementation("androidx.compose.material3:material3-window-size-class") {
            version { strictly(composeMaterial3Version) }
            because("material3 must stay aligned with the compose-foundation version from the Compose BOM")
        }
    }

    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    ktlintRuleset(libs.ktlint.compose)
    // AndroidX
    implementation(libs.androidx.appcompat) // AppCompatActivity
    implementation(libs.androidx.activity)
    implementation(libs.androidx.palette)
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.adaptive)
    implementation(libs.androidx.navigation3.viewmodel)

    implementation(libs.paging.common)
    implementation(libs.paging.compose.android)

    implementation(libs.kotlinx.serialization.json)

    // Compose
    implementation(libs.runtime.tracing)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)

    // Coil
    implementation(libs.coil.core)
    implementation(libs.coil.compose.core)
    implementation(libs.coil.network.ktor3)

    // Google
    implementation(libs.play.services.oss.licenses)
    implementation(libs.play.services.identity)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.basement)

    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    implementation("com.google.apis:google-api-services-drive:v3-rev20260428-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "commons-logging")
    }

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // 3rd party
    implementation(libs.okhttp)
    implementation(libs.process.phoenix)
    implementation(libs.koin.core)

    // ViewModel and LiveData
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.common.java8)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)

    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:context"))
    implementation(project(":lib:notification"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:playservices"))
    implementation(project(":lib:ktx"))
    implementation(project(":lib:permissions"))
    implementation(project(":playstore"))

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// ADD THIS AT THE BOTTOM
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")

val releaseGoogleServicesFile = providers.gradleProperty("APPWATCHER_GOOGLE_SERVICES_FILE")
    .map { file(it) }

val releaseGoogleServicesTasks = setOf(
    "processReleaseGoogleServices",
    "processBenchmarkGoogleServices",
    "processNonMinifiedReleaseGoogleServices"
)

afterEvaluate {
    tasks.withType<GoogleServicesTask>().configureEach {
        if (name in releaseGoogleServicesTasks) {
            googleServicesJsonFiles.set(releaseGoogleServicesFile.map { listOf(it) })
        }
    }
}

/**
 * Guards the invariant that `strictly` cannot express: that the resolved compose-material3 was
 * actually built against the compose-foundation the Compose BOM resolves.
 *
 * material3 1.5.0-alpha17 declares compose-foundation 1.11.x. Paired with the BOM's foundation
 * 1.12.0 that ABI skew crashed every Material3 TextField at measure time. `strictly` stops an
 * external dependency from overriding the pin; this stops the pin itself from being moved to an
 * alpha built against a different foundation minor line.
 */
abstract class VerifyComposeMaterial3Alignment : DefaultTask() {

    @get:Input
    abstract val rootComponent: Property<ResolvedComponentResult>

    @get:Input
    abstract val expectedMaterial3Version: Property<String>

    @get:Input
    abstract val skip: Property<Boolean>

    @TaskAction
    fun verify() {
        if (skip.get()) {
            logger.lifecycle("compose-material3 alignment check skipped via -PskipComposeVersionCheck")
            return
        }

        val components = collectComponents()
        val material3 = components.selectAny("androidx.compose.material3", "material3-android", "material3")
            ?: throw GradleException("androidx.compose.material3 is not on the runtime classpath")
        val foundation = components.selectAny("androidx.compose.foundation", "foundation-android", "foundation")
            ?: throw GradleException("androidx.compose.foundation is not on the runtime classpath")

        val selectedMaterial3 = material3.moduleVersion!!.version
        val expected = expectedMaterial3Version.get()
        if (selectedMaterial3 != expected) {
            throw GradleException(report("compose-material3 was overridden by another dependency.", selectedMaterial3, expected, null, null))
        }

        val declaredFoundation = material3.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .mapNotNull { it.requested as? ModuleComponentSelector }
            .firstOrNull { it.group == "androidx.compose.foundation" && (it.module == "foundation" || it.module == "foundation-android") }
            ?.version
            ?: return

        val resolvedFoundation = foundation.moduleVersion!!.version
        if (declaredFoundation.minorLine() != resolvedFoundation.minorLine()) {
            throw GradleException(
                report(
                    "compose-material3 is ABI-skewed against compose-foundation.",
                    selectedMaterial3,
                    expected,
                    declaredFoundation,
                    resolvedFoundation
                )
            )
        }
    }

    private fun report(headline: String, selectedMaterial3: String, expected: String, declaredFoundation: String?, resolvedFoundation: String?): String = buildString {
        appendLine(headline)
        appendLine()
        appendLine("  androidx.compose.material3:material3   -> $selectedMaterial3 (expected $expected)")
        if (declaredFoundation != null) {
            appendLine("    declares compose-foundation          -> $declaredFoundation")
            appendLine("  androidx.compose.foundation:foundation -> $resolvedFoundation")
        }
        appendLine()
        appendLine("material3 must be built against the same compose-foundation minor line the Compose BOM")
        appendLine("resolves. Otherwise every Material3 TextField crashes at measure time with")
        appendLine("NoSuchElementException in TextFieldMeasurePolicy, or AbstractMethodError on")
        appendLine("androidx.compose.foundation.style.CustomStyle.applyStyle. This compiles cleanly, so it")
        appendLine("only shows up on device.")
        appendLine()
        appendLine("Known offender: com.google.android.gms:play-services-oss-licenses hard-requires a")
        appendLine("material3 1.5.0 alpha for its Compose licenses UI, and a BOM constraint does not")
        appendLine("outrank a hard requirement. Update the `compose-material3` pin in")
        appendLine("gradle/libs.versions.toml to an alpha built against the resolved compose-foundation.")
        appendLine()
        append("Bypass with -PskipComposeVersionCheck=true (intended for reproducing the failure only).")
    }

    private fun collectComponents(): Map<String, ResolvedComponentResult> {
        val found = LinkedHashMap<String, ResolvedComponentResult>()
        val seen = HashSet<String>()
        val queue = ArrayDeque<ResolvedComponentResult>()
        queue.add(rootComponent.get())
        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!seen.add(component.id.displayName)) {
                continue
            }
            component.moduleVersion?.let { found.putIfAbsent("${it.group}:${it.name}", component) }
            component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { queue.add(it.selected) }
        }
        return found
    }

    private fun Map<String, ResolvedComponentResult>.selectAny(group: String, vararg names: String): ResolvedComponentResult? = names.firstNotNullOfOrNull { this["$group:$it"] }

    private fun String.minorLine(): String = substringBefore('-').split('.').take(2).joinToString(".")
}

val verifyComposeMaterial3Alignment = tasks.register<VerifyComposeMaterial3Alignment>("verifyComposeMaterial3Alignment") {
    group = "verification"
    description = "Fails if compose-material3 drifts off its pin or off the Compose BOM's compose-foundation."
    rootComponent.set(configurations.named("debugRuntimeClasspath").flatMap { it.incoming.resolutionResult.rootComponent })
    expectedMaterial3Version.set(composeMaterial3Version)
    skip.set(providers.gradleProperty("skipComposeVersionCheck").map { it.toBoolean() }.orElse(false))
}

tasks.named("check") {
    dependsOn(verifyComposeMaterial3Alignment)
}