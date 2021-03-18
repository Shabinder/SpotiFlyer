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

import com.willowtreeapps.fuzzywuzzy.ToStringFunction
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.partialRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.ratio
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.tokenSetPartialRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.tokenSetRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.tokenSortPartialRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch.tokenSortRatio
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


class WeightedRatio : BasicAlgorithm() {


    override fun apply(s1: String, s2: String, stringProcessor: ToStringFunction<String>): Int {
        var s1Copy = s1
        var s2Copy = s2

        s1Copy = stringProcessor.apply(s1Copy)
        s2Copy = stringProcessor.apply(s2Copy)

        val len1 = s1Copy.length
        val len2 = s2Copy.length

        if (len1 == 0 || len2 == 0) {
            return 0
        }

        var tryPartials = TRY_PARTIALS
        val unbaseScale = UNBASE_SCALE
        var partialScale = PARTIAL_SCALE

        val base = ratio(s1Copy, s2Copy)
        val lenRatio = max(len1, len2).toDouble() / min(len1, len2)

        // if strings are similar length don't use partials
        if (lenRatio < 1.5) tryPartials = false

        // if one string is much shorter than the other
        if (lenRatio > 8) partialScale = .6

        if (tryPartials) {

            val partial = partialRatio(s1Copy, s2Copy) * partialScale
            val partialSor = tokenSortPartialRatio(s1Copy, s2Copy) * unbaseScale * partialScale
            val partialSet = tokenSetPartialRatio(s1Copy, s2Copy) * unbaseScale * partialScale

            return round(max(max(max(base.toDouble(), partial), partialSor), partialSet)).toInt()

        } else {

            val tokenSort = tokenSortRatio(s1Copy, s2Copy) * unbaseScale
            val tokenSet = tokenSetRatio(s1Copy, s2Copy) * unbaseScale

            return round(max(max(base.toDouble(), tokenSort), tokenSet)).toInt()

        }

    }

    companion object {

        const val UNBASE_SCALE = .95
        const val PARTIAL_SCALE = .90
        const val TRY_PARTIALS = true
    }

}