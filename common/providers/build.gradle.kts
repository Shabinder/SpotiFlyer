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
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(project(":common:core-components"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
                implementation(Extras.youtubeDownloader)
                implementation(Extras.fuzzyWuzzy)
            }
        }
        androidMain {
            dependencies {
                implementation(Extras.mp3agic)
            }
        }
        desktopMain {
            dependencies {
                implementation(Extras.mp3agic)
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