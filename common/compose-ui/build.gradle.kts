plugins {
    id("multiplatform-compose-setup")
    id("android-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(MVIKotlin.mvikotlin)
                implementation(MVIKotlin.mvikotlinExtensionsReaktive)
                implementation(Badoo.Reaktive.reaktive)
                implementation(Decompose.decompose)
                implementation(Decompose.extensionsCompose)
            }
        }
    }
}
