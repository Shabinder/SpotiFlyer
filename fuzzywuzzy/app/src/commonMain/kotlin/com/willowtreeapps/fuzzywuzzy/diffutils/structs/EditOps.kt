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

package com.willowtreeapps.fuzzywuzzy.diffutils.structs

data class EditOp(

        var type: EditType? = null,
        var spos: Int = 0, // source block pos
        var dpos: Int = 0 // destination block pos
) {

    override fun toString(): String {
        return type!!.name + "(" + spos + "," + dpos + ")"
    }
}

class MatchingBlock {
    var spos: Int = 0
    var dpos: Int = 0
    var length: Int = 0

    override fun toString(): String {
        return "($spos,$dpos,$length)"
    }
}

class OpCode {

    var type: EditType? = null
    var sbeg: Int = 0
    var send: Int = 0
    var dbeg: Int = 0
    var dend: Int = 0

    override fun toString(): String {
        return (type!!.name + "(" + sbeg + "," + send + ","
                + dbeg + "," + dend + ")")
    }
}

enum class EditType {
    DELETE,
    EQUAL,
    INSERT,
    REPLACE,
    KEEP
}