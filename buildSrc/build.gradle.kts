plugins {
    `kotlin-dsl`
    //`kotlin-dsl-precompiled-script-plugins`
}

group = "com.shabinder"
version = "2.1"

repositories {
    jcenter()
    mavenLocal()
    google()
    maven(url = "https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.1")
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