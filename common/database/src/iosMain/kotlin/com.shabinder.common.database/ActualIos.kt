package com.shabinder.common.database

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogLogger
import com.shabinder.database.Database
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

@Suppress("RedundantNullableReturnType")
actual fun createDatabase(): Database? {
    val driver = NativeSqliteDriver(Database.Schema, "Database.db")
    return Database(driver)
}

actual fun getLogger(): Logger = NSLogLogger()