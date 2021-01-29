package com.shabinder.common.database

import android.content.Context
import co.touchlab.kermit.LogcatLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.DownloadRecordDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

lateinit var appContext: Context

actual fun createDb(): DownloadRecordDatabase {
    val driver = AndroidSqliteDriver(DownloadRecordDatabase.Schema, appContext, "DownloadRecordDatabase.db")
    return DownloadRecordDatabase(driver)
}
actual fun getLogger(): Logger = LogcatLogger()
