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

plugins {
    id("com.android.application")
    kotlin("android")
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
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.shabinder.spotiflyer"
        minSdkVersion(Versions.minSdkVersion)
        targetSdkVersion(Versions.targetSdkVersion)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
    }
    buildToolsVersion = "30.0.3"

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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

    implementation(project(":common:database"))
    implementation(project(":common:compose"))
    implementation(project(":common:root"))
    implementation(project(":common:dependency-injection"))
    implementation(project(":common:data-models"))

    implementation(Koin.android)
    implementation(Koin.compose)

    implementation("com.google.accompanist:accompanist-insets:0.7.1")

    //DECOMPOSE
    implementation(Decompose.decompose)
    implementation(Decompose.extensionsCompose)

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:27.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

/*
    //Lifecycle
    Versions.androidLifecycle.let{
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$it")
    }
*/


    Extras.Android.apply {
        implementation(appUpdator)
        implementation(razorpay)
        implementation(fetch)
    }
    implementation(MVIKotlin.mvikotlin)
    implementation(MVIKotlin.mvikotlinMain)
    implementation(MVIKotlin.mvikotlinLogging)
    implementation(MVIKotlin.mvikotlinTimeTravel)
    implementation(Decompose.decompose)
    implementation(Decompose.extensionsCompose)

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(Androidx.junit)
    androidTestImplementation(Androidx.expresso)

    //Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}