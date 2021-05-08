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
    id("multiplatform-setup")
    id("android-setup")
    id("kotlin-parcelize")
}

fun org.jetbrains.kotlin.gradle.dsl.KotlinNativeBinaryContainer.generateFramework() {
    framework {
        baseName = "SpotiFlyer"
        linkerOpts.add("-lsqlite3")
        export(project(":common:dependency-injection"))
        export(project(":common:data-models"))
        export(project(":common:database"))
        export(project(":common:main"))
        export(project(":common:list"))
        export(Decompose.decompose)
        export(MVIKotlin.mvikotlinMain)
        export(MVIKotlin.mvikotlinLogging)
    }
}

kotlin {

    /*IOS Target Can be only built on Mac*/
    if(HostOS.isMac) {
        val sdkName: String? = System.getenv("SDK_NAME")
        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("ios"){
                binaries {
                    generateFramework()
                }
            }
        } else {
            iosX64("ios"){
                binaries {
                    generateFramework()
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(project(":common:list"))
                implementation(project(":common:main"))
                implementation(SqlDelight.coroutineExtensions)
            }
        }
    }
    if(HostOS.isMac){
        /*Required to Export `packForXcode`*/
        sourceSets {
            named("iosMain") {
                dependencies {
                    api(project(":common:dependency-injection"))
                    api(project(":common:data-models"))
                    api(project(":common:database"))
                    api(project(":common:list"))
                    api(project(":common:main"))
                    api(Decompose.decompose)
                    api(MVIKotlin.mvikotlinMain)
                    api(MVIKotlin.mvikotlinLogging)
                }
            }
        }
    }
}

val packForXcode by tasks.creating(Sync::class) {
    if(HostOS.isMac){
        group = "build"
        val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
        val targetName = "ios"
        val framework = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(targetName)
            .binaries.getFramework(mode)
        inputs.property("mode", mode)
        dependsOn(framework.linkTask)
        val targetDir = File(buildDir, "xcode-frameworks")
        from(framework.outputDirectory)
        into(targetDir)
    }
}
