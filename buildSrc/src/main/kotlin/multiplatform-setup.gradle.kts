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
    id("android-setup")
    id("kotlin-multiplatform")
    id("org.jetbrains.compose")
    id("ktlint-setup")
    id("kotlin-parcelize")
    id("compiler-args")
}

kotlin {
    /*IOS Target Can be only built on Mac*/
    if (HostOS.isMac) {
        val sdkName: String? = System.getenv("SDK_NAME")
        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("ios")
        } else {
            iosX64("ios") {}
        }
    }

    jvm("desktop")
    android()

    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        // nodejs()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Deps.ktorBundle)
                implementation(Deps.kotlinxSerialization)
                implementation(Deps.kotlinCoroutines)
                implementation(Deps.mviKotlinBundle)
                implementation(Deps.decompose)
                implementation(Deps.koinCore)
            }
        }

        named("androidMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(Deps.androidXCommonBundle)
                implementation(Deps.decomposeComposeExt)
                implementation(Deps.ktorClientAndroidOkHttp)
                implementation(Deps.koinAndroidBundle)
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.desktop.common)
                implementation(compose.materialIconsExtended)
                implementation(Deps.decomposeComposeExt)
                implementation(Deps.ktorClientApache)
                implementation(Deps.slf4j)
            }
        }
        named("jsMain") {
            dependencies {
                implementation(Deps.ktorClientJS)
            }
        }
        if (HostOS.isMac) {
            named("iosMain") {
                dependencies {
                    implementation(Deps.ktorClientIOS)
                }
            }
        }
    }
}
