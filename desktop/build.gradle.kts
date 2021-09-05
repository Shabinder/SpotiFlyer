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
    id("ktlint-setup")
}

group = "com.shabinder"
version = Versions.versionName

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":common:database"))
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:core-components"))
                implementation(project(":common:data-models"))
                implementation(project(":common:compose"))
                implementation(project(":common:providers"))
                implementation(project(":common:root"))
                implementation("com.github.kokorin.jaffree:jaffree:2021.08.16")

                // Decompose
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)

                // MVI
                implementation(MVIKotlin.mvikotlin)
                implementation(MVIKotlin.mvikotlinMain)

                // Koin
                implementation(Koin.core)

                // Matomo
                implementation("org.piwik.java.tracking:matomo-java-tracker:1.6")
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
            modules("java.sql", "java.security.jgss", "jdk.crypto.ec")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpotiFlyer"
            copyright = "© 2021 Shabinder. All rights reserved."
            vendor = "Shabinder"

            val iconsRoot = project.file("src/jvmMain/resources/drawable")
            macOS {
                bundleID = "com.shabinder.spotiflyer"
                iconFile.set(iconsRoot.resolve("spotiflyer.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("spotiflyer.ico"))
                // Wondering what the heck is this? See : https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "ace223ed-3ffb-452c-bc90-082e9e0a6d5d"
                menuGroup = packageName
            }
            linux {
                iconFile.set(iconsRoot.resolve("spotiflyer.png"))
            }
        }
    }
}
