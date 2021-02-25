import org.jetbrains.compose.compose

plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
    id("kotlin-parcelize")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(project(":common:root"))
                implementation(project(":common:main"))
                implementation(project(":common:list"))
                implementation(project(":common:database"))
                implementation(project(":common:data-models"))
                implementation(project(":common:dependency-injection"))
                //DECOMPOSE
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
            }
        }
    }
}
