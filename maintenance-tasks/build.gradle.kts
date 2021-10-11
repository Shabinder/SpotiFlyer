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
    with(deps) {
        implementation(slf4j.simple)
        implementation(bundles.ktor)
        implementation(ktor.client.apache)
        implementation(kotlinx.serialization.json)

        // testDep
        testImplementation(kotlin.kotlinTestJunit)
    }
}

tasks.test {
    useJUnit()
}
