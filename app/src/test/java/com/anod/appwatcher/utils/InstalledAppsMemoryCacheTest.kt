package com.anod.appwatcher.utils

import info.anodsplace.framework.content.InstalledApps
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class InstalledAppsMemoryCacheTest {

    @Test
    fun `reset loads package info from a new cache generation`() {
        val versionCode = AtomicInteger(1)
        val reads = AtomicInteger()
        val cache = InstalledApps.MemoryCache(object : InstalledApps {
            override fun packageInfo(packageName: String): InstalledApps.Info {
                reads.incrementAndGet()
                val version = versionCode.get()
                return InstalledApps.Info(versionCode = version, versionName = version.toString())
            }
        })

        assertEquals(1, cache.packageInfo(PACKAGE_NAME).versionCode)
        versionCode.set(2)
        assertEquals(1, cache.packageInfo(PACKAGE_NAME).versionCode)

        cache.reset()

        assertEquals(2, cache.packageInfo(PACKAGE_NAME).versionCode)
        assertEquals(2, reads.get())
    }

    @Test
    fun `in flight read cannot populate cache generation created by reset`() {
        val firstReadStarted = CountDownLatch(1)
        val releaseFirstRead = CountDownLatch(1)
        val reads = AtomicInteger()
        val cache = InstalledApps.MemoryCache(object : InstalledApps {
            override fun packageInfo(packageName: String): InstalledApps.Info {
                val read = reads.incrementAndGet()
                if (read == 1) {
                    firstReadStarted.countDown()
                    check(releaseFirstRead.await(5, TimeUnit.SECONDS))
                }
                return InstalledApps.Info(versionCode = read, versionName = read.toString())
            }
        })
        val executor = Executors.newSingleThreadExecutor()

        try {
            val firstRead = executor.submit<InstalledApps.Info> {
                cache.packageInfo(PACKAGE_NAME)
            }
            check(firstReadStarted.await(5, TimeUnit.SECONDS))

            cache.reset()
            releaseFirstRead.countDown()

            assertEquals(1, firstRead.get(5, TimeUnit.SECONDS).versionCode)
            assertEquals(2, cache.packageInfo(PACKAGE_NAME).versionCode)
            assertEquals(2, reads.get())
        } finally {
            releaseFirstRead.countDown()
            executor.shutdownNow()
        }
    }

    private companion object {
        const val PACKAGE_NAME = "com.example.app"
    }
}