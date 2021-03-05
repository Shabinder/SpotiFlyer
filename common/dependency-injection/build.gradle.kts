import org.jetbrains.compose.compose

plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(project(":fuzzywuzzy:app"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation(Ktor.clientCore)
                implementation(Ktor.clientCio)
                implementation(Ktor.clientSerialization)
                implementation(Ktor.clientLogging)
                implementation(Ktor.clientJson)
                implementation(Ktor.auth)
                // koin
                api(Koin.core)
                api(Koin.test)

                api(Extras.kermit)
                api(Extras.youtubeDownloader)
                api(Extras.mp3agic)
            }
        }
        androidMain {
            dependencies{
                implementation(Ktor.clientAndroid)
                implementation(Extras.Android.fetch)
                implementation(Koin.android)
                implementation(Extras.Android.razorpay)
                //api(files("$rootDir/libs/mobile-ffmpeg.aar"))
            }
        }
        desktopMain {
            dependencies{
                implementation(Ktor.clientApache)
                implementation(Ktor.slf4j)
            }
        }
    }
}
