package com.shabinder.common.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.shabinder.database.TodoDatabase
import java.io.File

@Suppress("FunctionName") // FactoryFunction
fun TodoDatabaseDriver(): SqlDriver {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "ComposeTodoDatabase.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
    TodoDatabase.Schema.create(driver)

    return driver
}
