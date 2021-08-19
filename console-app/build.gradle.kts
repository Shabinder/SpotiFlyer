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
    implementation("org.piwik.java.tracking:matomo-java-tracker:1.6")

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

tasks.test {
    useJUnit()
}
