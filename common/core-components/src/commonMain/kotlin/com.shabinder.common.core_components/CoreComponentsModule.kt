package com.shabinder.common.core_components

import co.touchlab.kermit.Kermit
import com.russhwolf.settings.Settings
import com.shabinder.common.core_components.analytics.analyticsModule
import com.shabinder.common.core_components.file_manager.fileManagerModule
import com.shabinder.common.core_components.media_converter.mediaConverterModule
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.core_components.utils.createHttpClient
import com.shabinder.common.database.getLogger
import org.koin.dsl.module

fun coreComponentModules(enableLogging: Boolean) = listOf(
    commonModule(enableLogging),
    analyticsModule(),
    fileManagerModule(),
    mediaConverterModule()
)

private fun commonModule(enableLogging: Boolean) = module {
    single { createHttpClient(enableLogging) }
    single { Settings() }
    single { Kermit(getLogger()) }
    single { PreferenceManager(get()) }
}
