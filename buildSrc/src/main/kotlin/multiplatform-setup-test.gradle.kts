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
    id("compiler-args")
}

kotlin {
    /*IOS Target Can be only built on Mac*/
    if(HostOS.isMac){
        val sdkName: String? = System.getenv("SDK_NAME")
        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("ios")
        } else {
            iosX64("ios")
        }
    }

    jvm("desktop")
    android()

    js(BOTH) {
        browser()
        // nodejs()
    }
    sourceSets {
        named("commonTest") {
            dependencies {
                implementation(Deps.kotlinTestBundle)
            }
        }

        named("androidTest") {
            dependencies {
                implementation(Deps.kotlinJunitTest)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(Deps.kotlinJunitTest)
            }
        }
        named("jsTest") {
            dependencies {
                implementation(Deps.kotlinJSTest)
            }
        }
    }
}