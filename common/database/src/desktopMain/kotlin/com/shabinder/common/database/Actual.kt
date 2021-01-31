package com.shabinder.common.database

import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.Database
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

actual fun createDatabase(): Database {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "Database.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        .also { Database.Schema.create(it) }
    return Database(driver)
}
actual fun getLogger(): Logger = CommonLogger()