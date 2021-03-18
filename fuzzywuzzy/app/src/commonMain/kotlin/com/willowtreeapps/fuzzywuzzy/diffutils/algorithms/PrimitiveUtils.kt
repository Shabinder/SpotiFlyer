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

package com.willowtreeapps.fuzzywuzzy.diffutils.algorithms

internal object PrimitiveUtils {

    fun max(vararg elems: Double): Double {

        if (elems.isEmpty()) return 0.0

        var best = elems[0]

        for (t in elems) {
            if (t > best) {
                best = t
            }
        }

        return best

    }

    fun max(vararg elems: Int): Int {

        if (elems.size == 0) return 0

        var best = elems[0]

        for (t in elems) {
            if (t > best) {
                best = t
            }
        }

        return best

    }


}