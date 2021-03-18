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

import com.willowtreeapps.fuzzywuzzy.Ratio
import com.willowtreeapps.fuzzywuzzy.ToStringFunction


class TokenSort : RatioAlgorithm() {

    override fun apply(s1: String, s2: String, ratio: Ratio, stringFunction: ToStringFunction<String>): Int {

        val sorted1 = processAndSort(s1, stringFunction)
        val sorted2 = processAndSort(s2, stringFunction)

        return ratio.apply(sorted1, sorted2)

    }

    private fun processAndSort(input: String, stringProcessor: ToStringFunction<String>): String {
        var inputCopy = input

        inputCopy = stringProcessor.apply(inputCopy)
        val wordsArray = inputCopy.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val words = listOf(*wordsArray)
        val joined = Utils.sortAndJoin(words, " ")

        return joined.trim { it <= ' ' }

    }

}