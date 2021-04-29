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
    //kotlin("native.cocoapods")
}

// Required be cocoapods
version = "1.0"

kotlin {

    /*IOS Target Can be only built on Mac*/
    if(HostOS.isMac){
        val sdkName: String? = System.getenv("SDK_NAME")
        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("ios"){
                binaries {
                    framework {
                        baseName = "SpotiFlyer"
                        linkerOpts.add("-lsqlite3")
                        export(project(":common:database"))
                        export(project(":common:main"))
                        export(project(":common:list"))
                        export(project(":common:dependency-injection"))
                        export(project(":common:data-models"))
                        export(Decompose.decompose)
                        export(MVIKotlin.mvikotlin)
                    }
                }
            }
        } else {
            iosX64("ios"){
                binaries {
                    framework {
                        baseName = "SpotiFlyer"
                        linkerOpts.add("-lsqlite3")
                        export(project(":common:database"))
                        export(project(":common:main"))
                        export(project(":common:list"))
                        export(project(":common:dependency-injection"))
                        export(project(":common:data-models"))
                        export(Decompose.decompose)
                        export(MVIKotlin.mvikotlin)
                    }
                }
            }
        }
    }

    /*cocoapods {
        // Configure fields required by CocoaPods.
        summary = "SpotiFlyer Native Module"
        homepage = "https://github.com/Shabinder/SpotiFlyer"
        authors = "Shabinder Singh"
        // You can change the name of the produced framework.
        // By default, it is the name of the Gradle project.
        frameworkName = "SpotiFlyer"
        ios.deploymentTarget = "11.0"

        *//*pod("dependency_injection"){
            version = "1.0"
            source = path(rootProject.file("common/dependency-injection"))
        }*//*
    }*/

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:dependency-injection"))
                implementation(project(":common:data-models"))
                implementation(project(":common:database"))
                implementation(project(":common:list"))
                implementation(project(":common:main"))
                implementation(SqlDelight.coroutineExtensions)
                implementation(MVIKotlin.coroutines)
                implementation(MVIKotlin.mvikotlin)
                implementation(Decompose.decompose)
            }
        }
    }
    sourceSets {
        named("iosMain") {
            dependencies {
                api(project(":common:dependency-injection"))
                api(project(":common:data-models"))
                api(project(":common:database"))
                api(project(":common:list"))
                api(project(":common:main"))
                api(Decompose.decompose)
                api(MVIKotlin.mvikotlin)
            }
        }
    }
}

val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val targetName = "ios"
    val framework = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from(framework.outputDirectory)
    into(targetDir)
}
