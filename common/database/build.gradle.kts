plugins {
    id("multiplatform-setup")
    id("android-setup")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("DownloadRecordDatabase") {
        packageName = "com.shabinder.database"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(Deps.Badoo.Reaktive.reaktive)
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
