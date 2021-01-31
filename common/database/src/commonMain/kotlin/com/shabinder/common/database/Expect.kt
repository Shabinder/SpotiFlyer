package com.shabinder.common.database

import co.touchlab.kermit.Logger
import com.shabinder.database.Database

expect fun createDatabase() : Database
expect fun getLogger(): Logger