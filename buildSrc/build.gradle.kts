plugins {
    `kotlin-dsl`
    //`kotlin-dsl-precompiled-script-plugins`
}

group = "com.shabinder"
version = "2.1"

repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
    google()
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.2")
    implementation("com.google.gms:google-services:4.3.5")
    implementation("com.google.firebase:perf-plugin:1.3.5")
    implementation("com.google.firebase:firebase-crashlytics-gradle:2.5.1")
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