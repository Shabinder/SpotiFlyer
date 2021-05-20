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
}

android {
    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}

kotlin {
    /*IOS Target Can be only built on Mac*/
    if(HostOS.isMac){
        val sdkName: String? = System.getenv("SDK_NAME")
        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("ios")
        } else {
            iosX64("ios") {}
        }
    }

    jvm("desktop").compilations.all {
        kotlinOptions {
            useIR = true
        }
    }
    android().compilations.all {
        kotlinOptions {
            useIR = true
        }
    }
    js {
        /*
        * TODO Enable JS IR Compiler
        *  waiting for Decompose & MVI Kotlin to support same
        * */
        browser()
        // nodejs()
        binaries.executable()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                // Decompose
                implementation(Decompose.decompose)

                // MVI
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)

                // Koin
                implementation(Koin.core)

                implementation(Ktor.auth)
                implementation(Ktor.clientJson)
                implementation(Ktor.clientCore)
                implementation(Ktor.clientLogging)
                implementation(Ktor.clientSerialization)

                // Extras
                implementation(Extras.kermit)
                implementation("co.touchlab:stately-common:1.1.7")
                implementation("dev.icerock.moko:parcelize:0.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt") {
                    @Suppress("DEPRECATION")
                    isForce = true
                }
            }
        }

        named("androidMain") {
            dependencies {
                implementation("androidx.appcompat:appcompat:1.2.0")
                implementation(Androidx.core)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(Decompose.extensionsCompose)
                implementation(Ktor.clientAndroid)
                implementation(Koin.android)
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.desktop.common)
                implementation(compose.materialIconsExtended)
                implementation(Decompose.extensionsCompose)
                implementation(Ktor.clientApache)
                implementation(Ktor.slf4j)
            }
        }
        named("jsMain") {
            dependencies {
                implementation(Ktor.clientJs)
                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.30")
                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.115-kotlin-1.4.10")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.30")
            }
        }
        if(HostOS.isMac){
            named("iosMain"){
                dependencies {
                    implementation(Ktor.clientIos)
                }
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
