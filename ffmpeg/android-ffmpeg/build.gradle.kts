plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    //ndkVersion "22.0.7026061"
    compileSdk = Versions.compileSdkVersion
    buildToolsVersion = "30.0.3"

    defaultConfig {
        consumerProguardFile("proguard-rules.pro")

        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion

        /*ndk {
            abiFilters.addAll(setOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
        }*/
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources {
            excludes.apply {
                add("META-INF/*")
            }
            jniLibs.pickFirsts.apply {
                add("**/*.so")
            }
        }
    }
}

dependencies { /**/ }

