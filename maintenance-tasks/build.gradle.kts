plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("ktlint-setup")
    application
}

group = "com.shabinder"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
    applicationName = "maintenance"
}

dependencies {
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
