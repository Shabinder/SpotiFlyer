rootProject.name = "spotiflyer"
enableFeaturePreview("GRADLE_METADATA")

include(
    ":common:database",
    ":common:compose",
    ":common:root",
    ":common:main",
    ":common:list",
    ":common:data-models",
    ":common:dependency-injection",
    ":fuzzywuzzy:app",
    ":android",
    ":desktop",
    ":web-app"
)
