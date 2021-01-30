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