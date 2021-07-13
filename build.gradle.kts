/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jlleitschuh.gradle.ktlint-idea")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // mavenLocal()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
        maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        dependsOn(":common:data-models:generateI18n4kFiles")
        kotlinOptions { jvmTarget = "1.8" }
    }
    afterEvaluate {
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.let { kmpExt ->
            kmpExt.sourceSets.run {
                all {
                    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
                    languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
                }
                removeAll { it.name == "androidAndroidTestRelease" }
            }
        }
    }
}
