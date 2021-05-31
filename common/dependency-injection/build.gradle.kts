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
    id("android-setup")
    id("multiplatform-setup")
    id("multiplatform-setup-test")
    kotlin("plugin.serialization")
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation("org.jetbrains.kotlinx:atomicfu:0.16.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
                implementation("com.russhwolf:multiplatform-settings-no-arg:0.7.7")
                implementation(Extras.youtubeDownloader)
                implementation(Extras.fuzzyWuzzy)
                implementation(MVIKotlin.rx)
            }
        }
        androidMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(Extras.mp3agic)
                // implementation(files("$rootDir/libs/mobile-ffmpeg.aar"))
            }
        }
        desktopMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(Extras.mp3agic)
            }
        }
        jsMain {
            dependencies {
                implementation(npm("browser-id3-writer", "4.4.0"))
                implementation(npm("file-saver", "2.0.4"))
            }
        }
    }
}
