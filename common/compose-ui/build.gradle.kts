import org.jetbrains.compose.compose

plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.materialIconsExtended)
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                //implementation(MVIKotlin.rx)
                implementation(SqlDelight.coroutineExtensions)
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)
                //implementation(MVIKotlin.mvikotlinExtensionsReaktive)
                //implementation(Badoo.Reaktive.reaktive)
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.TheAnnotationYouWantToDisable"
        )
    }
}