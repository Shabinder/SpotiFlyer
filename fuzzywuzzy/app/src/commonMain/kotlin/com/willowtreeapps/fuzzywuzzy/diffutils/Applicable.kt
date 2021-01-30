package com.willowtreeapps.fuzzywuzzy.diffutils

/**
 * A ratio/algorithm that can be applied
 */

interface Applicable {

    /**
     * Apply the ratio/algorithm to the input strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The score of similarity
     */
    fun apply(s1: String, s2: String): Int

}