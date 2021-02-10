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
                //implementation("org.jetbrains.compose.material:material-icons-extended:0.3.0-build150")
                implementation(project(":common:dependency-injection"))
                //implementation("com.alialbaali.kamel:kamel-image:0.0.7")
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(SqlDelight.coroutineExtensions)
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)

                //Coil-Image Loading
                Versions.coilVersion.let{
                    implementation("dev.chrisbanes.accompanist:accompanist-coil:$it")
                    implementation("dev.chrisbanes.accompanist:accompanist-insets:$it")
                }
            }
        }
    }
}
