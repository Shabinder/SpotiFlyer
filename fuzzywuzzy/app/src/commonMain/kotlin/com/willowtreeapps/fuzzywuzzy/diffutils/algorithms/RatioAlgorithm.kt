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
import com.willowtreeapps.fuzzywuzzy.diffutils.ratio.SimpleRatio

abstract class RatioAlgorithm : BasicAlgorithm {

    var ratio: Ratio? = null

    constructor() : super() {
        this.ratio = SimpleRatio()
    }

    constructor(stringFunction: ToStringFunction<String>) : super(stringFunction) {}

    constructor(ratio: Ratio) : super() {
        this.ratio = ratio
    }


    constructor(stringFunction: ToStringFunction<String>, ratio: Ratio) : super(stringFunction) {
        this.ratio = ratio
    }

    abstract fun apply(s1: String, s2: String, ratio: Ratio, stringFunction: ToStringFunction<String>): Int

    fun with(ratio: Ratio): RatioAlgorithm {
        this.ratio = ratio
        return this
    }

    fun apply(s1: String, s2: String, ratio: Ratio): Int {
        return apply(s1, s2, ratio, stringFunction!!)
    }

    override fun apply(s1: String, s2: String, stringProcessor: ToStringFunction<String>): Int {
        return apply(s1, s2, ratio!!, stringProcessor)
    }
}
