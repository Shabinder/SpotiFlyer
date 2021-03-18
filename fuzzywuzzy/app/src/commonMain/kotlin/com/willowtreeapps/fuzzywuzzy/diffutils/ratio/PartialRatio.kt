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


/**
 * Partial ratio of similarity
 */
class PartialRatio : Ratio {

    /**
     * Computes a partial ratio between the strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The partial ratio
     */
    override fun apply(s1: String, s2: String): Int {

        val shorter: String
        val longer: String

        if (s1.length < s2.length) {

            shorter = s1
            longer = s2

        } else {

            shorter = s2
            longer = s1

        }

        val matchingBlocks = DiffUtils.getMatchingBlocks(shorter, longer)

        val scores = ArrayList<Double>()

        for (mb in matchingBlocks) {

            val dist = mb.dpos - mb.spos

            val longStart = if (dist > 0) dist else 0
            var longEnd = longStart + shorter.length

            if (longEnd > longer.length) longEnd = longer.length

            val longSubstr = longer.substring(longStart, longEnd)

            val ratio = DiffUtils.getRatio(shorter, longSubstr)

            if (ratio > .995) {
                return 100
            } else {
                scores.add(ratio)
            }

        }

        return round(100 * scores.maxOrNull()!!).toInt()

    }

    override fun apply(s1: String, s2: String, sp: ToStringFunction<String>): Int {
        return apply(sp.apply(s1), sp.apply(s2))
    }


}
