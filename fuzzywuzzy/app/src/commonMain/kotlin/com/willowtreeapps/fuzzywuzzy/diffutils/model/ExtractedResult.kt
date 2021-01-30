package com.willowtreeapps.fuzzywuzzy.diffutils.model

data class ExtractedResult(var string: String?, val score: Int, val index: Int) : Comparable<ExtractedResult> {

    override fun compareTo(other: ExtractedResult): Int {
        return this.score.compareTo(other.score)
    }

    override fun toString(): String {
        return "(string: $string, score: $score, index: $index)"
    }
}