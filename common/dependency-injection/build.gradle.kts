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
                implementation(project(":fuzzywuzzy:app"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
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
                api(Extras.jsonKlaxon)
                api(Extras.youtubeDownloader)
                //api(Extras.fuzzyWuzzy)
                //api("com.github.willowtreeapps:fuzzywuzzy-kotlin:v0.1.1")
            }
        }
        androidMain {
            dependencies{
                implementation(Ktor.clientAndroid)
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
