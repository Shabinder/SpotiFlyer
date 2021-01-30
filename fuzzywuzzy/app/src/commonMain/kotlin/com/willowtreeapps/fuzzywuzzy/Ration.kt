package com.willowtreeapps.fuzzywuzzy

import com.willowtreeapps.fuzzywuzzy.diffutils.Applicable

/**
 * Interface for the different ratios
 */
interface Ratio : Applicable {

    /**
     * Applies the ratio between the two strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return Integer representing ratio of similarity
     */
    override fun apply(s1: String, s2: String): Int

    /**
     * Applies the ratio between the two strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @param sp String processor to pre-process strings before calculating the ratio
     * @return Integer representing ratio of similarity
     */
    fun apply(s1: String, s2: String, sp: ToStringFunction<String>): Int

}