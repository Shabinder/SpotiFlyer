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

import Extras.Android.Acra
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
    // Remove jcenter as soon as following issue closes
    // https://github.com/matomo-org/matomo-sdk-android/issues/301
    jcenter()
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

    compileSdkVersion(Versions.compileSdkVersion)
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.shabinder.spotiflyer"
        minSdkVersion(Versions.minSdkVersion)
        targetSdkVersion(Versions.targetSdkVersion)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            // isShrinkResources = true
            if (props.containsKey("storeFileDir")) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        useIR = true
        jvmTarget = "1.8"
    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    configurations {
        "implementation" {
            exclude(group = "androidx.compose.animation")
            exclude(group = "androidx.compose.foundation")
            exclude(group = "androidx.compose.material")
            exclude(group = "androidx.compose.runtime")
            exclude(group = "androidx.compose.ui")
        }
    }
    packagingOptions {
        exclude("META-INF/*")
    }
}
dependencies {
    implementation(compose.material)
    implementation(compose.materialIconsExtended)
    implementation(Androidx.androidxActivity)

    // Project's SubModules
    implementation(project(":common:database"))
    implementation(project(":common:compose"))
    implementation(project(":common:root"))
    implementation(project(":common:dependency-injection"))
    implementation(project(":common:data-models"))

    // Koin
    implementation(Koin.android)
    implementation(Koin.compose)

    // DECOMPOSE
    implementation(Decompose.decompose)
    implementation(Decompose.extensionsCompose)

    // MVI
    implementation(MVIKotlin.mvikotlin)
    implementation(MVIKotlin.mvikotlinMain)
    implementation(MVIKotlin.mvikotlinLogging)
    implementation(MVIKotlin.mvikotlinTimeTravel)

    // Extras
    with(Extras.Android) {
        implementation(Acra.notification)
        implementation(Acra.http)
        implementation(appUpdator)
        implementation(matomo)
    }

    with(Versions.androidxLifecycle) {
        implementation("androidx.lifecycle:lifecycle-service:$this")
        implementation("androidx.lifecycle:lifecycle-common-java8:$this")
    }

    implementation(Extras.kermit)
    // implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("dev.icerock.moko:parcelize:${Versions.mokoParcelize}")
    implementation("com.github.shabinder:storage-chooser:2.0.4.45")
    implementation("com.google.accompanist:accompanist-insets:0.13.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(Androidx.junit)
    androidTestImplementation(Androidx.expresso)

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}
