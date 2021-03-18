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
import com.willowtreeapps.fuzzywuzzy.diffutils.Applicable


abstract class BasicAlgorithm : Applicable {

    var stringFunction: ToStringFunction<String>? = null
        internal set

    constructor() {
        this.stringFunction = DefaultStringFunction()
    }

    constructor(stringFunction: ToStringFunction<String>) {
        this.stringFunction = stringFunction
    }

    abstract fun apply(s1: String, s2: String, stringProcessor: ToStringFunction<String>): Int

    override fun apply(s1: String, s2: String): Int {

        return apply(s1, s2, this.stringFunction!!)

    }

    fun with(stringFunction: ToStringFunction<String>): BasicAlgorithm {
        this.stringFunction = stringFunction
        return this
    }

    fun noProcessor(): BasicAlgorithm {
        this.stringFunction = ToStringFunction.NO_PROCESS
        return this
    }
}