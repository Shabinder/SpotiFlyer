package com.shabinder.common.database

import com.shabinder.database.DownloadRecordDatabase
import co.touchlab.kermit.Logger

expect fun createDb() : DownloadRecordDatabase
expect fun getLogger(): Logger