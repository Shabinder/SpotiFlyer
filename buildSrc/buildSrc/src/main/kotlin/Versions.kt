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

@file:Suppress("MayBeConstant", "SpellCheckingInspection", "UnstableApiUsage")

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.getByType

object Versions {
    // App's Version (To be bumped at each update)
    const val versionName = "3.6.4"

    const val versionCode = 32

    // Android
    const val minSdkVersion = 21
    const val compileSdkVersion = 31
    const val targetSdkVersion = 29
}

object HostOS {
    // Host OS Properties
    private val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows", true)
    val isMac = hostOs.startsWith("Mac", true)
    val isLinux = hostOs.startsWith("Linux", true)
}

val Project.Deps: VersionCatalog get() = project.extensions.getByType<VersionCatalogsExtension>().named("deps")

val VersionCatalog.ktorBundle get() = findBundle("ktor").get()
val VersionCatalog.statelyBundle get() = findBundle("stately").get()
val VersionCatalog.androidXLifecycleBundle get() = findBundle("androidx-lifecycle").get()
val VersionCatalog.androidXCommonBundle get() = findBundle("androidx-common").get()
val VersionCatalog.kotlinTestBundle get() = findBundle("kotlin-test").get()
val VersionCatalog.sqldelightBundle get() = findBundle("sqldelight").get()
val VersionCatalog.mviKotlinBundle get() = findBundle("mviKotlin").get()
val VersionCatalog.essentyBundle get() = findBundle("essenty").get()
val VersionCatalog.koinAndroidBundle get() = findBundle("koin-android").get()
val VersionCatalog.kotlinJSWrappers get() = findBundle("kotlin-js-wrappers").get()

val VersionCatalog.kotlinJunitTest get() = findDependency("kotlin-kotlinTestJunit").get()
val VersionCatalog.kotlinJSTest get() = findDependency("kotlin-kotlinTestJs").get()
val VersionCatalog.kermit get() = findDependency("kermit").get()
val VersionCatalog.decompose get() = findDependency("decompose-dep").get()
val VersionCatalog.decomposeComposeExt get() = findDependency("decompose-extensions-compose").get()
val VersionCatalog.jaffree get() = findDependency("jaffree").get()

val VersionCatalog.ktlintGradle get() = findDependency("ktlint-gradle").get()
val VersionCatalog.androidGradle get() = findDependency("androidx-gradle-plugin").get()
val VersionCatalog.mosaicGradle get() = findDependency("mosaic-gradle").get()
val VersionCatalog.kotlinComposeGradle get() = findDependency("kotlin-compose-gradle").get()
val VersionCatalog.kotlinGradle get() = findDependency("kotlin-kotlinGradlePlugin").get()
val VersionCatalog.i18n4kGradle get() = findDependency("i18n4k-gradle-plugin").get()
val VersionCatalog.sqlDelightGradle get() = findDependency("sqldelight-gradle-plugin").get()
val VersionCatalog.kotlinSerializationPlugin get() = findDependency("kotlin-serialization").get()

val VersionCatalog.koinCore get() = findDependency("koin-core").get()
val VersionCatalog.kotlinCoroutines get() = findDependency("kotlin-coroutines").get()
val VersionCatalog.kotlinxSerialization get() = findDependency("kotlinx-serialization-json").get()
val VersionCatalog.ktorClientIOS get() = findDependency("ktor-client-ios").get()
val VersionCatalog.ktorClientAndroid get() = findDependency("ktor-client-android").get()
val VersionCatalog.ktorClientAndroidOkHttp get() = findDependency("ktor-client-okhttp").get()
val VersionCatalog.ktorClientApache get() = findDependency("ktor-client-apache").get()
val VersionCatalog.ktorClientJS get() = findDependency("ktor-client-js").get()
val VersionCatalog.ktorClientCIO get() = findDependency("ktor-client-cio").get()
val VersionCatalog.slf4j get() = findDependency("slf4j-simple").get()

val VersionCatalog.sqlDelightJDBC get() = findDependency("sqlite-jdbc-driver").get()
val VersionCatalog.sqlDelightNative get() = findDependency("sqldelight-native-driver").get()
val VersionCatalog.sqlDelightAndroid get() = findDependency("sqldelight-android-driver").get()
val VersionCatalog.sqlDelightDriver get() = findDependency("sqldelight-driver").get()
