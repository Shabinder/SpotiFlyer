import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.shabinder"
version = Versions.versionName

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":common:database"))
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:compose"))
                implementation(project(":common:root"))
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
                implementation(MVIKotlin.mvikotlin)
                implementation(MVIKotlin.mvikotlinMain)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpotiFlyer"
        }
    }
}