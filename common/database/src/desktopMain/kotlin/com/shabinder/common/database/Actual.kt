package com.shabinder.common.database

import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.DownloadRecordDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

actual fun createDb(): DownloadRecordDatabase {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "DownloadRecordDatabase.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        .also { DownloadRecordDatabase.Schema.create(it) }
    return DownloadRecordDatabase(driver)
}
actual fun getLogger(): Logger = CommonLogger()