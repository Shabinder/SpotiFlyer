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

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("deps") {
            from(files("buildSrc/deps.versions.toml"))
        }
    }
}

rootProject.name = "spotiflyer"

include(
    ":common:database",
    ":common:compose",
    ":common:root",
    ":common:main",
    ":common:list",
    ":common:preference",
    ":common:data-models",
    ":common:providers",
    ":common:core-components",
    ":common:dependency-injection",
    ":ffmpeg:android-ffmpeg",
    ":android",
    ":desktop",
    ":web-app",
    //":console-app",
    ":maintenance-tasks",
)
