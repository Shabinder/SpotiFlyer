plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm("desktop")
    android()
    // Remove After upgrading dependency
    /*configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group.contains("org.jetbrains.compose")) {
                useVersion(JetBrains.Compose.VERSION)
            }
            if (requested.group.contains("androidx.compose")) {
                useVersion(Versions.compose)
            }
        }
    }*/
    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
            }
        }

        named("androidMain") {
            dependencies {
                api("androidx.appcompat:appcompat:1.2.0")
                api(Androidx.core)
            }
        }

        named("desktopMain") {
            dependencies {
                api(compose.desktop.common)
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
