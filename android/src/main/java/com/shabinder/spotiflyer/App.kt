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
import android.content.Context
import com.shabinder.common.di.initKoin
import com.shabinder.common.translations.Strings
import com.shabinder.spotiflyer.di.appModule
import org.acra.config.httpSender
import org.acra.config.notification
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.logger.Level
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class App : Application(), KoinComponent {

    companion object {
        const val SIGNATURE_HEX = "53304f6d75736a2f30484230334c454b714753525763724259444d3d0a"
    }

    val tracker: Tracker by lazy {
        TrackerBuilder.createDefault(
            "https://matomo.spotiflyer.ml/matomo.php", 1
        )
            .build(Matomo.getInstance(this)).apply {
                if (BuildConfig.DEBUG) {
                    /*Timber.plant(DebugTree())
                    addTrackingCallback {
                        Timber.d(it.toMap().toString())
                        it
                    }*/
                }
            }
    }

    override fun onCreate() {
        super.onCreate()

        val loggingEnabled = true
        // KOIN - DI
        initKoin(loggingEnabled) {
            androidLogger(Level.NONE) // No virtual method elapsedNow
            androidContext(this@App)
            modules(appModule(loggingEnabled))
        }
    }

    @Suppress("SpellCheckingInspection")
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Crashlytics
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            /*
            * Prompt User Before Sending Any Crash Report
            * Obeying `F-Droid Inclusion Privacy Rules`
            * */
            notification {
                title = Strings.acraNotificationTitle()
                text = Strings.acraNotificationText()
                channelName = "SpotiFlyer_Crashlytics"
                channelDescription = "Notification Channel to send Spotiflyer Crashes."
                sendOnClick = true
            }
            // Send Crash Report to self hosted Acrarium (FOSS)
            httpSender {
                uri = "https://acrarium.spotiflyer.ml/acrarium/report"
                basicAuthLogin = "sDj2xCKQIxw0dujf"
                basicAuthPassword = "O83du0TsgsDJ69zN"
                httpMethod = HttpSender.Method.POST
                connectionTimeout = 15000
                socketTimeout = 20000
                compress = true
            }
        }
    }
}
