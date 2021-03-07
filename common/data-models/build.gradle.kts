plugins {
    id("multiplatform-setup")
    id("android-setup")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("dev.icerock.moko:parcelize:0.6.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
            }
        }
    }
}