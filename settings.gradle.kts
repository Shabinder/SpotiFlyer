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