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
                with(deps) {
                    api(multiplatform.settings)
                    api(kotlinx.atomicfu)
                    implementation(mviKotlin.rx)
                    implementation(decompose.dep)
                }
            }
        }
        androidMain {
            dependencies {
                with(deps) {
                    implementation(mp3agic)
                    implementation(countly.android)
                }
                implementation(project(":ffmpeg:android-ffmpeg"))
            }
        }
        desktopMain {
            dependencies {
                with(deps) {
                    implementation(mp3agic)
                    implementation(countly.desktop)
                    implementation(jaffree)
                }
            }
        }
        jsMain {
            dependencies {
                implementation(npm("browser-id3-writer", "4.4.0"))
                implementation(npm("file-saver", "2.0.4"))
                implementation(deps.kotlin.js.wrappers.ext)
            }
        }
    }
}