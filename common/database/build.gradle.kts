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
    id("multiplatform-setup-test")
    id("android-setup")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("Database") {
        packageName = "com.shabinder.database"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:data-models"))
                // implementation(Badoo.Reaktive.reaktive)
                // SQL Delight
                implementation(SqlDelight.runtime)
                implementation(SqlDelight.coroutineExtensions)
                api(Extras.kermit)
            }
        }

        androidMain {
            dependencies {
                implementation(SqlDelight.androidDriver)
            }
        }

        desktopMain {
            dependencies {
                implementation(SqlDelight.sqliteDriver)
                implementation(SqlDelight.jdbcDriver)
            }
        }

        /*iosMain {
            dependencies {
                implementation(Deps.Squareup.SQLDelight.nativeDriver)
            }
        }*/
    }
}
