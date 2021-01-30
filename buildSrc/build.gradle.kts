plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

group = "com.shabinder"
version = "2.1"

buildscript{
    repositories {
        // TODO: remove after new build is published
        mavenLocal()
        google()
        jcenter()
        maven(url = "https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.2")
        classpath(JetBrains.Compose.gradlePlugin)
        classpath(JetBrains.Kotlin.gradlePlugin)
    }
}

repositories {
    maven(url = "https://jitpack.io")
    jcenter()
    mavenLocal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.2")
    implementation(JetBrains.Compose.gradlePlugin)
    implementation(JetBrains.Kotlin.gradlePlugin)
    implementation(JetBrains.Kotlin.serialization)
    implementation(SqlDelight.gradlePlugin)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}