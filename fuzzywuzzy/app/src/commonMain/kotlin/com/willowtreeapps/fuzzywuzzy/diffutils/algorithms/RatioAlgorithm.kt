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
