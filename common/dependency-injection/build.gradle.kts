plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation("org.kodein.di:kodein-di:${Versions.kodein}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
                implementation(Extras.jsonKlaxon)
                implementation(Ktor.clientCore)
                implementation(Ktor.clientCio)
                implementation(Ktor.clientJson)
                implementation(Ktor.clientSerialization)
                implementation(Ktor.auth)
            }
        }
        androidMain {
            dependencies{
                implementation(Ktor.clientAndroid)

            }
        }
        desktopMain {
            dependencies{
                //implementation(Ktor.clientDesktop)
            }
        }
    }
}
