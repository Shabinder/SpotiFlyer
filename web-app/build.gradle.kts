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
    kotlin("js")
}

group = "com.shabinder"
version = "0.1"

dependencies {
    with(deps) {
        implementation(koin.core)
        implementation(decompose.dep)
        implementation(ktor.client.js)
        with(bundles) {
            implementation(mviKotlin)
            implementation(ktor)
            implementation(kotlin.js.wrappers)
        }
    }
    implementation(project(":common:root"))
    implementation(project(":common:main"))
    implementation(project(":common:list"))
    implementation(project(":common:database"))
    implementation(project(":common:data-models"))
    implementation(project(":common:providers"))
    implementation(project(":common:core-components"))
    implementation(project(":common:dependency-injection"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${deps.kotlin.kotlinGradlePlugin.get().versionConstraint.requiredVersion}")
}

kotlin {
    js(IR) {
        //useCommonJs()
        browser {
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
    // WorkAround: https://youtrack.jetbrains.com/issue/KT-49124
    rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
        rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
            resolution("@webpack-cli/serve", "1.5.2")
        }
    }
}