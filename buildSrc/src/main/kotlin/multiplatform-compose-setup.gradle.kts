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

plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    id("org.jetbrains.compose")
    id("kotlin-parcelize")
    id("ktlint-setup")
}

kotlin {
    jvm("desktop")
    android()
    sourceSets {
        named("commonMain") {
            dependencies {
                // Decompose
                implementation(Decompose.decompose)

                // MVI
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)

                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.animation)

                implementation(Extras.kermit)
                implementation("dev.icerock.moko:parcelize:0.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-native-mt") {
                    @Suppress("DEPRECATION")
                    isForce = true
                }
            }
        }
        named("androidMain") {
            dependencies {
                implementation("androidx.appcompat:appcompat:1.3.0")
                implementation(Androidx.core)
            }
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}
