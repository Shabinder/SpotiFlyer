import gradle.kotlin.dsl.accessors._2e23d8fadf0ed92ae13e19db3d83f86d.compose
import gradle.kotlin.dsl.accessors._2e23d8fadf0ed92ae13e19db3d83f86d.kotlin
import gradle.kotlin.dsl.accessors._2e23d8fadf0ed92ae13e19db3d83f86d.sourceSets
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.compose

plugins {
//    id("com.android.library")
    id("android-setup")
    id("kotlin-multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm("desktop")
    android()
    js() {
        browser()
        //nodejs()
        binaries.executable()
    }
    sourceSets {
        named("commonMain") {
            dependencies {

            }
        }

        named("androidMain") {
            dependencies {
                implementation("androidx.appcompat:appcompat:1.2.0")
                implementation(Androidx.core)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.desktop.common)
                implementation(compose.materialIconsExtended)
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
            }
        }
        named("jsMain") {
            dependencies {
                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.30")
                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.115-kotlin-1.4.10")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.30")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
