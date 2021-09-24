@file:Suppress("UnstableApiUsage")

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
    id("ktlint-setup")
    id("compiler-args")
}

android {
    compileSdk = Versions.compileSdkVersion

    defaultConfig {
        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}
