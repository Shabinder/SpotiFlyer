plugins {
    kotlin("js")
    //id("org.jetbrains.kotlin.js") version "1.4.31"
}

group = "com.shabinder"
version = "0.1"

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(Decompose.decompose)
    implementation(Koin.core)
    implementation(MVIKotlin.mvikotlin)
    implementation(MVIKotlin.mvikotlinMain)
    implementation(project(":common:root"))
    implementation(project(":common:main"))
    implementation(project(":common:list"))
    implementation(project(":common:database"))
    implementation(project(":common:data-models"))
    implementation(project(":common:dependency-injection"))
    implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.115-kotlin-1.4.10")
    implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-pre.148-kotlin-1.4.30")
}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}