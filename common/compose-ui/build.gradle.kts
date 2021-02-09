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
/*
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        //useIR = true
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies",
            "-Xallow-unstable-dependencies","-Xskip-prerelease-check",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.TheAnnotationYouWantToDisable"
        )
    }
}*/
