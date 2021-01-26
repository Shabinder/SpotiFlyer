plugins {
    id("multiplatform-setup")
    id("android-setup")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("TodoDatabase") {
        packageName = "com.shabinder.database"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(Deps.Badoo.Reaktive.reaktive)
            }
        }

        androidMain {
            dependencies {
                implementation(SqlDelight.androidDriver)
                implementation(SqlDelight.sqliteDriver)
            }
        }

        desktopMain {
            dependencies {
                implementation(SqlDelight.sqliteDriver)
            }
        }

        /*iosMain {
            dependencies {
                implementation(Deps.Squareup.SQLDelight.nativeDriver)
            }
        }*/
    }
}
