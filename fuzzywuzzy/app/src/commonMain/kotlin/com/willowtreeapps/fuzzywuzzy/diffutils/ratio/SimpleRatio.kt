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

package com.willowtreeapps.fuzzywuzzy.diffutils.ratio

import com.willowtreeapps.fuzzywuzzy.Ratio
import com.willowtreeapps.fuzzywuzzy.ToStringFunction
import com.willowtreeapps.fuzzywuzzy.diffutils.DiffUtils
import kotlin.math.round

class SimpleRatio : Ratio {

    /**
     * Computes a simple Levenshtein distance ratio between the strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The resulting ratio of similarity
     */
    override fun apply(s1: String, s2: String): Int {

        return round(100 * DiffUtils.getRatio(s1, s2)).toInt()

    }

    override fun apply(s1: String, s2: String, sp: ToStringFunction<String>): Int {
        return apply(sp.apply(s1), sp.apply(s2))
    }
}