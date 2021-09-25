plugins {
    id("multiplatform-setup")
    id("multiplatform-setup-test")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                api("org.jetbrains.kotlinx:atomicfu:0.16.3")
                api(MultiPlatformSettings.dep)
                implementation(MVIKotlin.rx)
            }
        }
        androidMain {
            dependencies {
                implementation(Extras.mp3agic)
                implementation(Extras.Android.countly)
                implementation(project(":ffmpeg:android-ffmpeg"))
            }
        }
        desktopMain {
            dependencies {
                implementation(Extras.mp3agic)
                implementation(Extras.Desktop.countly)
                implementation("com.github.kokorin.jaffree:jaffree:2021.08.16")
            }
        }
        jsMain {
            dependencies {
                implementation(npm("browser-id3-writer", "4.4.0"))
                implementation(npm("file-saver", "2.0.4"))
            }
        }
    }
}