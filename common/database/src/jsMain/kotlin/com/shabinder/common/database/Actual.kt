package com.shabinder.common.database

import co.touchlab.kermit.CommonLogger
import co.touchlab.kermit.Logger
import com.shabinder.database.Database

actual fun createDatabase(): Database? = null
actual fun getLogger(): Logger = CommonLogger()