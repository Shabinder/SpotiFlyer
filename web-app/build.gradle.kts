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
    kotlin("js")
}

group = "com.shabinder"
version = "0.1"

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(Koin.core)
    implementation(Extras.kermit)
    implementation(Decompose.decompose)
    implementation(MVIKotlin.mvikotlin)
    implementation(MVIKotlin.coroutines)
    implementation(MVIKotlin.mvikotlinMain)
    implementation(MVIKotlin.mvikotlinLogging)
    implementation(project(":common:root"))
    implementation(project(":common:main"))
    implementation(project(":common:list"))
    implementation(project(":common:database"))
    implementation(project(":common:data-models"))
    implementation(project(":common:dependency-injection"))
    implementation("co.touchlab:stately-common:1.1.7")
    implementation("dev.icerock.moko:parcelize:${Versions.mokoParcelize}")
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1") {
    //  https://youtrack.jetbrains.com/issue/KTOR-2670
        isForce = true
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-native-mt") {
        @Suppress("DEPRECATION")
        isForce = true
    }
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.213-kotlin-1.5.10")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.224-kotlin-1.5.21")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-pre.213-kotlin-1.5.10")
}

kotlin {
    js(IR) {
        //useCommonJs()
        browser {
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}