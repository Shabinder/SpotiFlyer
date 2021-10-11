plugins {
    id("multiplatform-setup")
    id("multiplatform-setup-test")
    kotlin("plugin.serialization")
}

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    sourceSets {
        commonMain {
            dependencies {
                with(deps) {
                    implementation(project(":common:data-models"))
                    implementation(project(":common:database"))
                    implementation(project(":common:core-components"))
                    implementation(youtube.downloader)
                    implementation(fuzzy.wuzzy)
                    implementation(kotlinx.datetime)
                }
            }
        }
        androidMain {
            dependencies {
                implementation(deps.mp3agic)
            }
        }
        desktopMain {
            dependencies {
                implementation(deps.mp3agic)
                implementation(deps.jaffree)
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