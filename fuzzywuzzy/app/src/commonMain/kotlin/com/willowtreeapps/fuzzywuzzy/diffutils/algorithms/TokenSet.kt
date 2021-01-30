package com.willowtreeapps.fuzzywuzzy.diffutils.algorithms

import com.willowtreeapps.fuzzywuzzy.Ratio
import com.willowtreeapps.fuzzywuzzy.ToStringFunction

class TokenSet : RatioAlgorithm() {

    override fun apply(s1: String, s2: String, ratio: Ratio, stringFunction: ToStringFunction<String>): Int {
        var s1Copy = s1
        var s2Copy = s2

        s1Copy = stringFunction.apply(s1Copy)
        s2Copy = stringFunction.apply(s2Copy)

        val tokens1 = Utils.tokenizeSet(s1Copy)
        val tokens2 = Utils.tokenizeSet(s2Copy)

        val intersection = SetUtils.intersection(tokens1, tokens2)
        val diff1to2 = SetUtils.difference(tokens1, tokens2)
        val diff2to1 = SetUtils.difference(tokens2, tokens1)

        val sortedInter = Utils.sortAndJoin(intersection, " ").trim()
        val sorted1to2 = (sortedInter + " " + Utils.sortAndJoin(diff1to2, " ")).trim { it <= ' ' }
        val sorted2to1 = (sortedInter + " " + Utils.sortAndJoin(diff2to1, " ")).trim { it <= ' ' }

        val results = ArrayList<Int>()

        results.add(ratio.apply(sortedInter, sorted1to2))
        results.add(ratio.apply(sortedInter, sorted2to1))
        results.add(ratio.apply(sorted1to2, sorted2to1))

        return results.max()!!

    }

}