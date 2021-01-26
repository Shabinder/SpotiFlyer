pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "spotiflyer"
enableFeaturePreview("GRADLE_METADATA")

include(
    ":common:database",
    ":common:compose-ui",
    ":common:data-models",
    ":common:dependency-injection",
    ":android",
    ":desktop"
)