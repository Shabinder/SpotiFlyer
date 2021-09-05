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

@file:JsModule("file-saver")
@file:JsNonModule

package com.shabinder.common.core_components

import org.w3c.files.Blob

external interface FileSaverOptions {
    var autoBom: Boolean
}

external fun saveAs(data: Blob, filename: String = definedExternally, options: FileSaverOptions = definedExternally)

external fun saveAs(data: Blob)

external fun saveAs(data: Blob, filename: String = definedExternally)

external fun saveAs(data: String, filename: String = definedExternally, options: FileSaverOptions = definedExternally)

external fun saveAs(data: String)

external fun saveAs(data: String, filename: String = definedExternally)

external fun saveAs(data: Blob, filename: String = definedExternally, disableAutoBOM: Boolean = definedExternally)

external fun saveAs(data: String, filename: String = definedExternally, disableAutoBOM: Boolean = definedExternally)
