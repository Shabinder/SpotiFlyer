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
}

kotlin {
    jvm("desktop")
    android()
    //ios()
    js() {
        browser()
        //nodejs()
        binaries.executable()
    }
    sourceSets {
        named("commonTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testCommon)
                implementation(JetBrains.Kotlin.testAnnotationsCommon)
            }
        }

        named("androidTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testJunit)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testJunit)
            }
        }
        named("jsTest") {
            dependencies {

            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
