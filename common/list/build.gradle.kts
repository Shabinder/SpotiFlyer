plugins {
    id("multiplatform-setup")
    id("android-setup")
    id("kotlin-parcelize")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(SqlDelight.coroutineExtensions)
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)
                implementation(Decompose.decompose)
            }
        }
    }
}
