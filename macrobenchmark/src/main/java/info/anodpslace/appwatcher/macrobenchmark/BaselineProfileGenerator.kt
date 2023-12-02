package info.anodpslace.appwatcher.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup(): Unit = baselineProfileRule.collect(
        packageName = "com.anod.appwatcher",
        profileBlock = {
            startActivityAndWait()
        }
    )
}
