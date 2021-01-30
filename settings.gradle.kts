pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "spotiflyer"
enableFeaturePreview("GRADLE_METADATA")

include(
    ":common:database",
    ":common:compose-ui",
    ":common:data-models",
    ":common:dependency-injection",
    ":fuzzywuzzy:app",
    ":android",
    ":desktop"
)