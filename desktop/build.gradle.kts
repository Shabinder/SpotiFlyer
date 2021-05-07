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
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.shabinder"
version = Versions.versionName

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "14"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":common:database"))
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:compose"))
                implementation(project(":common:data-models"))
                implementation(project(":common:root"))
                // Decompose
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)

                // MVI
                implementation(MVIKotlin.mvikotlin)
                implementation(MVIKotlin.mvikotlinMain)

                // Koin
                implementation(Koin.core)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        description = "Music Downloader for Spotify, Gaana, Youtube Music."
        nativeDistributions {
            modules("java.sql", "java.security.jgss")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpotiFlyer"
        }
    }
}