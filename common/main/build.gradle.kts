import org.jetbrains.compose.compose

plugins {
    id("multiplatform-setup")
    id("android-setup")
    id("kotlin-parcelize")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(project(":common:dependency-injection"))
                //implementation("com.alialbaali.kamel:kamel-image:0.1.0")
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(SqlDelight.coroutineExtensions)
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
            }
        }
    }
}
