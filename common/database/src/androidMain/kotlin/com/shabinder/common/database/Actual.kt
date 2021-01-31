package com.shabinder.common.database

import android.content.Context
import co.touchlab.kermit.LogcatLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver

lateinit var appContext: Context

actual fun createDatabase(): Database {
    val driver = AndroidSqliteDriver(Database.Schema, appContext, "Database.db")
    return Database(driver)
}
actual fun getLogger(): Logger = LogcatLogger()
