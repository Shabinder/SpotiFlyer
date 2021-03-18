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

import android.annotation.SuppressLint
import android.content.Context
import co.touchlab.kermit.LogcatLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver

lateinit var appContext: Context

/*
* As MainActivity is God Activity , hence its active almost throughout App's lifetime
* */
@SuppressLint("StaticFieldLeak")
lateinit var activityContext: Context

@Suppress("RedundantNullableReturnType")
actual fun createDatabase(): Database? {
    val driver = AndroidSqliteDriver(Database.Schema, appContext, "Database.db")
    return Database(driver)
}
actual fun getLogger(): Logger = LogcatLogger()