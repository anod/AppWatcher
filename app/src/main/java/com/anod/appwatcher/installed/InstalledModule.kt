package com.anod.appwatcher.installed

import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun createInstalledModule(): Module = module {
    factory { (viewModelScope: CoroutineScope) -> ChangelogAdapter(viewModelScope, get(), get(), getKoin()) }
    factory { ImportBulkManager(getKoin()) }
    factoryOf(::ImportInstalledTask)
}