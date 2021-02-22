plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("dev.icerock.moko:parcelize:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
            }
        }
    }
}