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
    tasks.named<Copy>("jvmProcessResources") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    sourceSets {
        val jvmMain by getting {
            resources.srcDirs("../common/data-models/src/main/res")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":common:database"))
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:core-components"))
                implementation(project(":common:data-models"))
                implementation(project(":common:compose"))
                implementation(project(":common:providers"))
                implementation(project(":common:root"))

                with(deps) {
                    implementation(jaffree)

                    with(decompose) {
                        implementation(dep)
                        implementation(extensions.compose)
                    }
                    with(mviKotlin) {
                        implementation(dep)
                        implementation(main)
                    }

                    implementation(koin.core)
                }
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        description = "Music Downloader for Spotify, Gaana, Jio Saavn, Youtube Music."
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
                // https://www.guidgen.com/
                upgradeUuid = "50dac393-a24f-48a6-89c6-9218b24a5291"
                menuGroup = packageName
            }
            linux {
                iconFile.set(iconsRoot.resolve("spotiflyer.png"))
            }
        }
    }
}
