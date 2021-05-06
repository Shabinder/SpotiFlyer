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
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

group = "com.shabinder"
version = Versions.versionName

repositories {
    google()
}

android {
    val props = gradleLocalProperties(rootDir)

    if(props.containsKey("storeFileDir")) {
        signingConfigs {
            create("release") {
                storeFile = file(props.getProperty("storeFileDir"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    compileSdkVersion(29)
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
            isShrinkResources = true
            if(props.containsKey("storeFileDir")){
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
        coreLibraryDesugaringEnabled = true
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

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:27.1.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // Extras
    Extras.Android.apply {
        implementation(appUpdator)
        implementation(razorpay)
    }

    implementation("dev.icerock.moko:parcelize:0.6.1")
    implementation("com.github.shabinder:storage-chooser:2.0.4.45")
    implementation("com.google.accompanist:accompanist-insets:0.9.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(Androidx.junit)
    androidTestImplementation(Androidx.expresso)

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}