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
    id("compiler-args")
}

kotlin {
    jvm("desktop")
    android()
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("androidx.compose.animation")
            }
        }
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
                implementation("dev.icerock.moko:parcelize:${Versions.mokoParcelize}")
                implementation(JetBrains.Kotlin.coroutines) {
                    @Suppress("DEPRECATION")
                    isForce = true
                }
            }
        }
        named("androidMain") {
            dependencies {
                implementation(Androidx.androidxActivity)
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
