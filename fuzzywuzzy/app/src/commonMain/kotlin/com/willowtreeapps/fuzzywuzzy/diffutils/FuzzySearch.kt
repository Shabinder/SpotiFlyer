package com.willowtreeapps.fuzzywuzzy.diffutils

import com.willowtreeapps.fuzzywuzzy.ToStringFunction
import com.willowtreeapps.fuzzywuzzy.diffutils.algorithms.TokenSet
import com.willowtreeapps.fuzzywuzzy.diffutils.algorithms.TokenSort
import com.willowtreeapps.fuzzywuzzy.diffutils.algorithms.WeightedRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.model.BoundExtractedResult
import com.willowtreeapps.fuzzywuzzy.diffutils.model.ExtractedResult
import com.willowtreeapps.fuzzywuzzy.diffutils.ratio.PartialRatio
import com.willowtreeapps.fuzzywuzzy.diffutils.ratio.SimpleRatio

/**
 * FuzzySearch facade class
 */
object FuzzySearch {

    /**
     * Calculates a Levenshtein simple ratio between the strings.
     * This is indicates a measure of similarity
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The simple ratio
     */
    fun ratio(s1: String, s2: String): Int {

        return SimpleRatio().apply(s1, s2)

    }

    /**
     * Calculates a Levenshtein simple ratio between the strings.
     * This is indicates a measure of similarity
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The simple ratio
     */
    fun ratio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return SimpleRatio().apply(s1, s2, stringFunction)

    }

    /**
     * Inconsistent substrings lead to problems in matching. This ratio
     * uses a heuristic called "best partial" for when two strings
     * are of noticeably different lengths.
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The partial ratio
     */
    fun partialRatio(s1: String, s2: String): Int {

        return PartialRatio().apply(s1, s2)

    }

    /**
     * Inconsistent substrings lead to problems in matching. This ratio
     * uses a heuristic called "best partial" for when two strings
     * are of noticeably different lengths.
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The partial ratio
     */
    fun partialRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return PartialRatio().apply(s1, s2, stringFunction)

    }

    /**
     * Find all alphanumeric tokens in the string and sort
     * those tokens and then take ratio of resulting
     * joined strings.
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The partial ratio of the strings
     */
    fun tokenSortPartialRatio(s1: String, s2: String): Int {

        return TokenSort().apply(s1, s2, PartialRatio())

    }

    /**
     * Find all alphanumeric tokens in the string and sort
     * those tokens and then take ratio of resulting
     * joined strings.
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The partial ratio of the strings
     */
    fun tokenSortPartialRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return TokenSort().apply(s1, s2, PartialRatio(), stringFunction)

    }

    /**
     * Find all alphanumeric tokens in the string and sort
     * those tokens and then take ratio of resulting
     * joined strings.
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The full ratio of the strings
     */
    fun tokenSortRatio(s1: String, s2: String): Int {

        return TokenSort().apply(s1, s2, SimpleRatio())

    }

    /**
     * Find all alphanumeric tokens in the string and sort
     * those tokens and then take ratio of resulting
     * joined strings.
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The full ratio of the strings
     */
    fun tokenSortRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return TokenSort().apply(s1, s2, SimpleRatio(), stringFunction)

    }


    /**
     * Splits the strings into tokens and computes intersections and remainders
     * between the tokens of the two strings. A comparison string is then
     * built up and is compared using the simple ratio algorithm.
     * Useful for strings where words appear redundantly.
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The ratio of similarity
     */
    fun tokenSetRatio(s1: String, s2: String): Int {

        return TokenSet().apply(s1, s2, SimpleRatio())

    }

    /**
     * Splits the strings into tokens and computes intersections and remainders
     * between the tokens of the two strings. A comparison string is then
     * built up and is compared using the simple ratio algorithm.
     * Useful for strings where words appear redundantly.
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The ratio of similarity
     */
    fun tokenSetRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return TokenSet().apply(s1, s2, SimpleRatio(), stringFunction)

    }

    /**
     * Splits the strings into tokens and computes intersections and remainders
     * between the tokens of the two strings. A comparison string is then
     * built up and is compared using the simple ratio algorithm.
     * Useful for strings where words appear redundantly.
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The ratio of similarity
     */
    fun tokenSetPartialRatio(s1: String, s2: String): Int {

        return TokenSet().apply(s1, s2, PartialRatio())

    }

    /**
     * Splits the strings into tokens and computes intersections and remainders
     * between the tokens of the two strings. A comparison string is then
     * built up and is compared using the simple ratio algorithm.
     * Useful for strings where words appear redundantly.
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The ratio of similarity
     */
    fun tokenSetPartialRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return TokenSet().apply(s1, s2, PartialRatio(), stringFunction)

    }

    /**
     * Calculates a weighted ratio between the different algorithms for best results
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The ratio of similarity
     */
    fun weightedRatio(s1: String, s2: String): Int {

        return WeightedRatio().apply(s1, s2)

    }

    /**
     * Calculates a weighted ratio between the different algorithms for best results
     *
     * @param s1              Input string
     * @param s2              Input string
     * @param stringFunction Functor which transforms strings before
     * calculating the ratio
     * @return The ratio of similarity
     */
    fun weightedRatio(s1: String, s2: String, stringFunction: ToStringFunction<String>): Int {

        return WeightedRatio().apply(s1, s2, stringFunction)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult]  which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @return A list of the results
     */
    fun extractTop(query: String, choices: Collection<String>,
                   func: Applicable, limit: Int, cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)
        return extractor.extractTop(query, choices, func, limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param limit   Limits the number of results and speeds up
     * the search (k-top heap sort) is used
     * @param cutoff  Rejects any entries with score below this
     * @return A list of the results
     */
    fun extractTop(query: String, choices: Collection<String>,
                   limit: Int, cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)
        return extractor.extractTop(query, choices, WeightedRatio(), limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @param limit   The number of results to return
     * @return A list of the results
     */
    fun extractTop(query: String, choices: Collection<String>,
                   func: Applicable, limit: Int): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, func, limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param limit   The number of results to return
     * @return A list of the results
     */
    fun extractTop(query: String, choices: Collection<String>,
                   limit: Int): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, WeightedRatio(), limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @return A list of the results
     */
    fun extractSorted(query: String, choices: Collection<String>, func: Applicable): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, func)

    }


    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun extractSorted(query: String, choices: Collection<String>, func: Applicable,
                      cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)

        return extractor.extractTop(query, choices, func)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @return A list of the results
     */
    fun extractSorted(query: String, choices: Collection<String>): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, WeightedRatio())

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun extractSorted(query: String, choices: Collection<String>,
                      cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)

        return extractor.extractTop(query, choices, WeightedRatio())

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @return A list of the results
     */
    fun extractAll(query: String, choices: Collection<String>, func: Applicable): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractWithoutOrder(query, choices, func)

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun extractAll(query: String, choices: Collection<String>, func: Applicable,
                   cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)

        return extractor.extractWithoutOrder(query, choices, func)

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @return A list of the results
     */
    fun extractAll(query: String, choices: Collection<String>): List<ExtractedResult> {

        val extractor = Extractor()

        return extractor.extractWithoutOrder(query, choices, WeightedRatio())

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun extractAll(query: String, choices: Collection<String>, cutoff: Int): List<ExtractedResult> {

        val extractor = Extractor(cutoff)

        return extractor.extractWithoutOrder(query, choices, WeightedRatio())

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query   A string to match against
     * @param choices A list of choices
     * @param func    Scoring function
     * @return An object containing the best match and it's score
     */
    fun extractOne(query: String, choices: Collection<String>, func: Applicable): ExtractedResult {

        val extractor = Extractor()

        return extractor.extractOne(query, choices, func)

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query   A string to match against
     * @param choices A list of choices
     * @return An object containing the best match and it's score
     */
    fun extractOne(query: String, choices: Collection<String>): ExtractedResult {

        val extractor = Extractor()

        return extractor.extractOne(query, choices, WeightedRatio())

    }

    /**
     * Creates a **sorted** list of [ExtractedResult]  which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @return A list of the results
     */
    fun <T> extractTop(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, func: Applicable,
                       limit: Int, cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)
        return extractor.extractTop(query, choices, toStringFunction, func, limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param limit   Limits the number of results and speeds up
     * the search (k-top heap sort) is used
     * @param cutoff  Rejects any entries with score below this
     * @return A list of the results
     */
    fun <T> extractTop(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, limit: Int, cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)
        return extractor.extractTop(query, choices, toStringFunction, WeightedRatio(), limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @param limit   The number of results to return
     * @return A list of the results
     */
    fun <T> extractTop(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, func: Applicable,
                       limit: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, toStringFunction, func, limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param limit   The number of results to return
     * @return A list of the results
     */
    fun <T> extractTop(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, limit: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, toStringFunction, WeightedRatio(), limit)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @return A list of the results
     */
    fun <T> extractSorted(query: String, choices: Collection<T>,
                          toStringFunction: ToStringFunction<T>, func: Applicable): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, toStringFunction, func)

    }


    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun <T> extractSorted(query: String, choices: Collection<T>,
                          toStringFunction: ToStringFunction<T>, func: Applicable,
                          cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)

        return extractor.extractTop(query, choices, toStringFunction, func)

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @return A list of the results
     */
    fun <T> extractSorted(query: String, choices: Collection<T>,
                          toStringFunction: ToStringFunction<T>): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractTop(query, choices, toStringFunction, WeightedRatio())

    }

    /**
     * Creates a **sorted** list of [ExtractedResult] which contain all the choices
     * with their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun <T> extractSorted(query: String, choices: Collection<T>,
                          toStringFunction: ToStringFunction<T>, cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)

        return extractor.extractTop(query, choices, toStringFunction, WeightedRatio())

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @return A list of the results
     */
    fun <T> extractAll(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, func: Applicable): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractWithoutOrder(query, choices, toStringFunction, func)

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    The scoring function
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun <T> extractAll(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, func: Applicable,
                       cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)

        return extractor.extractWithoutOrder(query, choices, toStringFunction, func)

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @return A list of the results
     */
    fun <T> extractAll(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>): List<BoundExtractedResult<T>> {

        val extractor = Extractor()

        return extractor.extractWithoutOrder(query, choices, toStringFunction, WeightedRatio())

    }

    /**
     * Creates a list of [ExtractedResult] which contain all the choices with
     * their corresponding score where higher is more similar
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param cutoff  Keep only scores above cutoff
     * @return A list of the results
     */
    fun <T> extractAll(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, cutoff: Int): List<BoundExtractedResult<T>> {

        val extractor = Extractor(cutoff)

        return extractor.extractWithoutOrder(query, choices, toStringFunction, WeightedRatio())

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query   A string to match against
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @param func    Scoring function
     * @return An object containing the best match and it's score
     */
    fun <T> extractOne(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>, func: Applicable): BoundExtractedResult<T> {

        val extractor = Extractor()

        return extractor.extractOne(query, choices, toStringFunction, func)

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query   A string to match against
     * @param choices A list of choices
     * @param toStringFunction The ToStringFunction to be applied to all choices.
     * @return An object containing the best match and it's score
     */
    fun <T> extractOne(query: String, choices: Collection<T>,
                       toStringFunction: ToStringFunction<T>): BoundExtractedResult<T> {

        val extractor = Extractor()

        return extractor.extractOne(query, choices, toStringFunction, WeightedRatio())

    }


}
