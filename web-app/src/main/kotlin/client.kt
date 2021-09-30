/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import co.touchlab.kermit.Kermit
import com.shabinder.common.core_components.analytics.AnalyticsManager
import com.shabinder.common.core_components.file_manager.FileManager
import com.shabinder.common.core_components.preference_manager.PreferenceManager
import com.shabinder.common.di.ApplicationInit
import com.shabinder.common.di.initKoin
import com.shabinder.common.providers.FetchPlatformQueryResult
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import react.dom.render

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            App {
                dependencies = AppDependencies
            }
        }
    }
}

object AppDependencies : KoinComponent {
    val logger: Kermit
    val fileManager: FileManager
    val fetchPlatformQueryResult: FetchPlatformQueryResult
    val preferenceManager: PreferenceManager
    val analyticsManager: AnalyticsManager
    val appInit: ApplicationInit
    init {
        initKoin()
        fileManager = get()
        logger = get()
        fetchPlatformQueryResult = get()
        preferenceManager = get()
        analyticsManager = get()
        appInit = get()
    }
}