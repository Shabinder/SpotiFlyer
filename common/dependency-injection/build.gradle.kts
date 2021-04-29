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
    kotlin("native.cocoapods")
}

version = "1.0"

kotlin {

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "SpotiFlyer Native Module"
        homepage = "https://github.com/Shabinder/SpotiFlyer"
        authors = "Shabinder Singh"
        // You can change the name of the produced framework.
        // By default, it is the name of the Gradle project.
        frameworkName = "SpotiFlyer"
        ios.deploymentTarget = "13.5"

        // Dependencies
        pod("TagLibIOS") {
            version = "~> 0.3"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation("org.jetbrains.kotlinx:atomicfu:0.16.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.0")
                implementation("com.shabinder.fuzzywuzzy:fuzzywuzzy:1.0")
                implementation(Ktor.clientCore)
                implementation(Ktor.clientSerialization)
                implementation(Ktor.clientLogging)
                implementation(Ktor.clientJson)
                implementation(Ktor.auth)
                api(Extras.youtubeDownloader)
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
                implementation(Extras.Android.razorpay)
                api(Extras.mp3agic)
                api(Extras.jaudioTagger)
                api("com.github.shabinder:storage-chooser:2.0.4.45")
                // api(files("$rootDir/libs/mobile-ffmpeg.aar"))
            }
        }
        desktopMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(Ktor.clientApache)
                implementation(Ktor.slf4j)
                api(Extras.mp3agic)
                api(Extras.jaudioTagger)
            }
        }
        jsMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(Ktor.clientJs)
                implementation(npm("browser-id3-writer", "4.4.0"))
                implementation(npm("file-saver", "2.0.4"))
            }
        }
    }
}
