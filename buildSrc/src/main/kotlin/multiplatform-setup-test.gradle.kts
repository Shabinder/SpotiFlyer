plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
}

kotlin {
    jvm("desktop")
    android()
    //ios()
    js() {
        browser()
        //nodejs()
        binaries.executable()
    }
    sourceSets {
        named("commonTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testCommon)
                implementation(JetBrains.Kotlin.testAnnotationsCommon)
            }
        }

        named("androidTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testJunit)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(JetBrains.Kotlin.testJunit)
            }
        }
        named("jsTest") {
            dependencies {

            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
