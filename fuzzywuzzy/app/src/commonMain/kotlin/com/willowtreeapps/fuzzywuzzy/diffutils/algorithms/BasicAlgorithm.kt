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