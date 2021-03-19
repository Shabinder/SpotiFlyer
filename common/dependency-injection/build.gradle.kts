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

import org.jetbrains.compose.compose

plugins {
    id("multiplatform-setup")
    id("android-setup")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(project(":fuzzywuzzy:app"))
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation(Ktor.clientCore)
                implementation(Ktor.clientSerialization)
                implementation(Ktor.clientLogging)
                implementation(Ktor.clientJson)
                implementation(Ktor.auth)
                // koin
                api(Koin.core)
                api(Koin.test)
                api(Extras.kermit)
            }
        }
        androidMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(Koin.android)
                implementation(Ktor.clientAndroid)
                implementation(Extras.Android.fetch)
                implementation(Extras.Android.razorpay)
                api(Extras.youtubeDownloader)
                api(Extras.mp3agic)
                // api(files("$rootDir/libs/mobile-ffmpeg.aar"))
            }
        }
        desktopMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(Ktor.clientApache)
                implementation(Ktor.slf4j)
                api(Extras.youtubeDownloader)
                api(Extras.mp3agic)
            }
        }
        jsMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(Ktor.clientJs)
                implementation(npm("browser-id3-writer", "4.4.0"))
                implementation(npm("file-saver", "2.0.4"))
                // implementation(npm("@types/file-saver","2.0.1",generateExternals = true))
            }
        }
    }
}
