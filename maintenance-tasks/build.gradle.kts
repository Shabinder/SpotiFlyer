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
    implementation(Extras.fuzzyWuzzy)
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")
    implementation("io.ktor:ktor-client-core:1.5.4")
    implementation("io.ktor:ktor-client-apache:1.5.4")
    implementation("io.ktor:ktor-client-logging:1.6.0")
    implementation(Ktor.slf4j)
    implementation("io.ktor:ktor-client-serialization:1.5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    // testDeps
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}
