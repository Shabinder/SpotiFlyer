package com.shabinder.common.database

import com.squareup.sqldelight.db.SqlDriver

@Suppress("FunctionName")
expect fun TestDatabaseDriver(): SqlDriver
