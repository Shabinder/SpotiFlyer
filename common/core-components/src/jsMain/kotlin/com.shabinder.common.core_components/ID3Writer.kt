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

package com.shabinder.common.core_components

import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob

@JsModule("browser-id3-writer")
@JsNonModule
external class ID3Writer(a: ArrayBuffer) {
    fun setFrame(frameName: String, frameValue: Any): ID3Writer
    fun removeTag()
    fun addTag(): ArrayBuffer
    fun getBlob(): Blob
    fun getURL(): String
    fun revokeURL()
}
