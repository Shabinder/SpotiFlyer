package com.shabinder.common.database

import android.content.Context
import com.shabinder.database.DownloadRecordDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(DownloadRecordDatabase.Schema, context, "DownloadRecordDatabase.db")
    }
}
