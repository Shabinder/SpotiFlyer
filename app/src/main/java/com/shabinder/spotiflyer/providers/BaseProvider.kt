/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.providers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.di.Directories
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.utils.removeIllegalChars
import com.shabinder.spotiflyer.worker.ForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

abstract class BaseProvider {
    @Inject @ApplicationContext protected lateinit var ctx : Context
    @Inject protected lateinit var databaseDAO: DatabaseDAO
    @Inject protected lateinit var directories: Directories

    /*
    * Sending a Result from this method as null means Some Error Occurred!
    * */
    abstract suspend fun query(fullLink:String):PlatformQueryResult?

    protected val defaultDir
        get() = directories.defaultDir()
    protected val imageDir
        get() = directories.imageDir()

    protected fun finalOutputDir(itemName:String ,type:String, subFolder:String,defaultDir:String,extension:String = ".mp3" ): String =
        defaultDir + removeIllegalChars(type) + File.separator +
                if(subFolder.isEmpty())"" else { removeIllegalChars(subFolder) + File.separator} +
                removeIllegalChars(itemName) + extension

}