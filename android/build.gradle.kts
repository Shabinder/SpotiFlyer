import org.jetbrains.compose.compose

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
}

group = "com.shabinder"
version = Versions.versionName

repositories {
    google()
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.shabinder.android"
        minSdkVersion(Versions.minSdkVersion)
        targetSdkVersion(Versions.targetSdkVersion)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            //isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        //coreLibraryDesugaringEnabled = true
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
    /*buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }*/
}
dependencies {
    implementation(compose.material)
    implementation(compose.materialIconsExtended)
    implementation(Androidx.androidxActivity)

    implementation(project(":common:database"))
    implementation(project(":common:compose-ui"))
    implementation(project(":common:dependency-injection"))
    implementation(project(":common:data-models"))

    implementation(Koin.android)
    implementation(Koin.androidViewModel)

    //DECOMPOSE
    implementation(Decompose.decompose)
    implementation(Decompose.extensionsCompose)

/*
    //Lifecycle
    Versions.androidLifecycle.let{
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$it")
        implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$it")
    }

    //Coil-Image Loading
    Versions.coilVersion.let{
        implementation("dev.chrisbanes.accompanist:accompanist-coil:$it")
        implementation("dev.chrisbanes.accompanist:accompanist-insets:$it")
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
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation(Androidx.junit)
    androidTestImplementation(Androidx.expresso)

    //Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf(
            "-Xallow-jvm-ir-dependencies","-Xallow-unstable-dependencies",
            "-Xskip-prerelease-check",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}