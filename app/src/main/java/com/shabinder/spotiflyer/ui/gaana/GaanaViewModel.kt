/*
 * Copyright (C)  2020  Shabinder Singh
 *
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.ui.gaana

import androidx.hilt.lifecycle.ViewModelInject
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.networking.GaanaInterface
import com.shabinder.spotiflyer.utils.BaseViewModel

class GaanaViewModel @ViewModelInject constructor(val databaseDAO: DatabaseDAO) : BaseViewModel(){

    override var folderType:String = ""
    override var subFolder:String = ""
    var gaanaInterface : GaanaInterface? = null

    fun gaanaSearch(type:String,link:String){

    }

}