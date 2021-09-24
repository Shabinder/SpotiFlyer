plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.useExperimentalAnnotation("androidx.compose.animation")
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}