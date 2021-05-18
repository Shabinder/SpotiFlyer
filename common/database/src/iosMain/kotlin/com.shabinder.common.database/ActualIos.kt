package com.shabinder.common.database

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogLogger
import com.shabinder.database.Database
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.koin.dsl.module

@Suppress("RedundantNullableReturnType")
actual fun databaseModule() = module {
    single {
        val driver = NativeSqliteDriver(Database.Schema, "Database.db")
        SpotiFlyerDatabase(Database(driver))
    }
}

actual fun getLogger(): Logger = NSLogLogger()
