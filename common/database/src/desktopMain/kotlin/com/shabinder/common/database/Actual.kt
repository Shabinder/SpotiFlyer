/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.database

import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.Database
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

@Suppress("RedundantNullableReturnType")
actual fun createDatabase(): Database? {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "Database.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        .also { Database.Schema.create(it) }
    return Database(driver)
}
actual fun getLogger(): Logger = CommonLogger()
