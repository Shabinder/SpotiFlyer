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

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.compose.compose

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("org.jetbrains.compose")
    id("ktlint-setup")
}

group = "com.shabinder"
version = Versions.versionName

repositories {
    google()
    mavenCentral()
}

android {
    val props = gradleLocalProperties(rootDir)
    if (props.containsKey("storeFileDir")) {
        signingConfigs {
            create("release") {
                storeFile = file(props.getProperty("storeFileDir"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    compileSdk = Versions.compileSdkVersion
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.shabinder.spotiflyer"
        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        ndkVersion = "21.4.7075529"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            // isShrinkResources = true
            if (props.containsKey("storeFileDir")) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/*")
    }
}
dependencies {
    implementation(compose.material)
    implementation(compose.materialIconsExtended)
    implementation(deps.androidx.activity)

    // Project's SubModules
    implementation(project(":common:database"))
    implementation(project(":common:compose"))
    implementation(project(":common:root"))
    implementation(project(":common:dependency-injection"))
    implementation(project(":common:data-models"))
    implementation(project(":common:core-components"))
    implementation(project(":common:providers"))

    with(deps) {

        // Koin
        with(koin) {
            implementation(androidx.compose)
            implementation(android)
        }

        // DECOMPOSE
        with(decompose) {
            implementation(dep)
            implementation(extensions.compose)
        }

        implementation(countly.android)
        implementation(android.app.notifier)
        implementation(storage.chooser)

        with(bundles) {
            implementation(ktor)
            implementation(mviKotlin)
            implementation(androidx.lifecycle)
            implementation(accompanist.inset)
        }

        // Test
        testImplementation(junit)
        androidTestImplementation(androidx.junit)
        androidTestImplementation(androidx.expresso)

        // Desugar
        coreLibraryDesugaring(androidx.desugar)

        // Debug
        debugImplementation(leak.canary)
    }
}
