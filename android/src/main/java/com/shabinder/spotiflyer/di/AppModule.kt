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

package com.shabinder.spotiflyer.di

import com.shabinder.common.database.appContext
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import org.koin.dsl.module

val appModule = module {
    single { createFetchInstance() }
}

private fun createFetchInstance():Fetch{
    val fetchConfiguration =
        FetchConfiguration.Builder(appContext).run {
            setNamespace("ForegroundDownloaderService")
            setDownloadConcurrentLimit(4)
            build()
        }

    return Fetch.run {
        setDefaultInstanceConfiguration(fetchConfiguration)
        getDefaultInstance()
    }.apply {
        removeAll() //Starting fresh
    }
}