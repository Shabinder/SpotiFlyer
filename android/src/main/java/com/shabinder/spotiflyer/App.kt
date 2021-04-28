/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer

import android.app.Application
import com.shabinder.common.database.appContext
import com.shabinder.common.di.initKoin
import com.shabinder.spotiflyer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent

class App: Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        appContext = this
        val loggingEnabled = true

        initKoin(loggingEnabled) {
            androidLogger()
            androidContext(this@App)
            modules(appModule(loggingEnabled))
        }
    }
}