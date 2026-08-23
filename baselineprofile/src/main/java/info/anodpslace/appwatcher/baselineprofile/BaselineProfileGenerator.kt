package info.anodpslace.appwatcher.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates the startup and critical user journey baseline profile for the app.
 *
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 *
 * Expect `baseline-prof.txt` and `startup-prof.txt` to differ. Through build 17107 a single
 * collector wrote both, so git stored them as one blob and the startup profile carried nothing
 * the baseline profile did not -- dex layout had nothing to differentiate on. [startup] now feeds
 * the startup profile alone. After regenerating, `git ls-tree` on the profile directory must show
 * two distinct blob ids where every release through 17107 showed one: baseline-prof grows by
 * several thousand lines while startup-prof barely moves. That diff is the fix working, not a
 * broken generator.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Startup only, and the only generator that feeds the startup profile.
     *
     * The startup profile drives dex layout, so it pays off only while it describes what a cold
     * start actually touches. Folding the journeys below into it would move search, scrolling and
     * the drawer into the primary dex region and push genuine startup classes out of it, which
     * works against the metric the profile exists to improve.
     */
    @Test
    fun startup() {
        val packageName = targetPackageName()
        rule.collect(
            packageName = packageName,

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            AppWatcherJourney(scope = this, packageName = packageName).awaitWatchList()
        }
    }

    /**
     * The journeys that dominate real usage, collected into the baseline profile but deliberately
     * kept out of the startup profile.
     */
    @Test
    fun criticalUserJourneys() {
        val packageName = targetPackageName()
        rule.collect(
            packageName = packageName,
            includeInStartupProfile = false
        ) {
            pressHome()
            startActivityAndWait()

            val journey = AppWatcherJourney(scope = this, packageName = packageName)
            journey.awaitWatchList()
            journey.searchWatchList()
            journey.scrollWatchList()
            journey.visitDrawerScreen(titleResName = "navdrawer_item_add")
            journey.visitDrawerScreen(titleResName = "installed")
            journey.visitDrawerScreen(titleResName = "navdrawer_item_wishlist")
        }
    }

    /** The application id for the running build variant, passed as an instrumentation argument. */
    private fun targetPackageName(): String =
        InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: throw Exception("targetAppId not passed as instrumentation runner arg")
}