
enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("deps") {
            from(files("deps.versions.toml"))
        }
    }
}

rootProject.name = "spotiflyer-build"
