package com.willowtreeapps.fuzzywuzzy.diffutils.algorithms

internal object SetUtils {

    fun <T> intersection(s1: Set<T>, s2: Set<T>): Set<T> {

        val intersection = HashSet(s1)
        intersection.retainAll(s2)

        return intersection

    }

    fun <T> difference(s1: Set<T>, s2: Set<T>): Set<T> {

        val difference = HashSet(s1)
        difference.removeAll(s2)

        return difference

    }

}