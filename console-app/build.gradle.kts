plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("ktlint-setup")
    id("com.jakewharton.mosaic")
    application
}

group = "com.shabinder"
version = Versions.versionCode

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
    applicationName = "spotiflyer-console-app"
}

dependencies {
    with(deps) {
        implementation(Koin.core)
        implementation(project(":common:database"))
        implementation(project(":common:data-models"))
        implementation(project(":common:dependency-injection"))
        implementation(project(":common:root"))
        implementation(project(":common:main"))
        implementation(project(":common:list"))
        implementation(project(":common:list"))

        // Decompose
        implementation(Decompose.decompose)
        implementation(Decompose.extensionsCompose)

        // MVI
        implementation(MVIKotlin.mvikotlin)
        implementation(MVIKotlin.mvikotlinMain)

        // Koin
        implementation(Koin.core)

        // Matomo

        implementation(Ktor.slf4j)
        implementation(Ktor.clientCore)
        implementation(Ktor.clientJson)
        implementation(Ktor.clientApache)
        implementation(Ktor.clientLogging)
        implementation(Ktor.clientSerialization)
        implementation(Serialization.json)
        // testDeps
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.21")
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs.plus(
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
            )
        )
    }
}
tasks.test {
    useJUnit()
}
