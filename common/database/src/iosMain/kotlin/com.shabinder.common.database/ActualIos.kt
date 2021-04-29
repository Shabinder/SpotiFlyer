package com.shabinder.common.database

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogLogger
import com.shabinder.database.Database
import org.koin.dsl.module
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

@Suppress("RedundantNullableReturnType")
actual fun databaseModule(): Module {
    single {
        val driver = NativeSqliteDriver(Database.Schema, "Database.db")
        SpotiFlyerDatabase(Database(driver))
    }
}

actual fun getLogger(): Logger = NSLogLogger()