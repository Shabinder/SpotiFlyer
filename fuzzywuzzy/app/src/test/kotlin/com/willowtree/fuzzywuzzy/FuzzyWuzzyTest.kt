package com.willowtreeapps.fuzzywuzzy


import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import com.willowtreeapps.fuzzywuzzy.diffutils.ratio.SimpleRatio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FuzzyWuzzyTest {


    val choices = listOf("google", "bing", "facebook", "linkedin", "twitter", "googleplus", "bingnews", "plexoogl")
    val moreChoices = listOf("Atlanta Falcons", "New York Jets", "New York Giants", "Dallas Cowboys")


    @Test
    fun testRatio() {
        assertEquals(76, FuzzySearch.ratio("mysmilarstring", "mymostsimilarstsdsdring"))
        assertEquals(72, FuzzySearch.ratio("mysmilarstring", "myawfullysimilarstirng"))
        assertEquals(97, FuzzySearch.ratio("mysmilarstring", "mysimilarstring"))
        assertEquals(75, FuzzySearch.ratio("csr", "c s r"))

    }

    @Test
    fun testPartialRatio() {

        assertEquals(71, FuzzySearch.partialRatio("similar", "somewhresimlrbetweenthisstring"))
        assertEquals(43, FuzzySearch.partialRatio("similar", "notinheresim"))
        assertEquals(38, FuzzySearch.partialRatio("pros holdings, inc.", "settlement facility dow corning trust"))
        assertEquals(33, FuzzySearch.partialRatio("Should be the same", "Opposite ways go alike"))
        assertEquals(33, FuzzySearch.partialRatio("Opposite ways go alike", "Should be the same"))

    }

    @Test
    fun testTokenSortPartial() {

        assertEquals(67, FuzzySearch.tokenSortPartialRatio("mvn", "wwwwww.mavencentral.comm"))
        assertEquals(100, FuzzySearch.tokenSortPartialRatio("  order words out of ", "  words out of order"))
        assertEquals(44, FuzzySearch.tokenSortPartialRatio("Testing token set ratio token", "Added another test"))

    }

    @Test
    fun testTokenSortRatio() {
        assertEquals(84, FuzzySearch.tokenSortRatio("fuzzy was a bear", "fuzzy fuzzy was a bear"))

    }

    @Test
    fun testTokenSetRatio() {

        assertEquals(100, FuzzySearch.tokenSetRatio("fuzzy fuzzy fuzzy bear", "fuzzy was a bear"))
        assertEquals(39, FuzzySearch.tokenSetRatio("Testing token set ratio token", "Added another test"))

    }

    @Test
    fun testTokenSetPartial() {

        assertEquals(11, FuzzySearch.tokenSetPartialRatio("fuzzy was a bear", "blind 100"))
        assertEquals(67, FuzzySearch.partialRatio("chicago transit authority", "cta"))

    }

    @Test
    fun testWeightedRatio() {


        assertEquals(60, FuzzySearch.weightedRatio("mvn", "wwwwww.mavencentral.comm"))
        assertEquals(40, FuzzySearch.weightedRatio("mvn", "www'l34.423.4/23.4/234//////www.mavencentral.comm"))
        assertEquals(97, FuzzySearch.weightedRatio("The quick brown fox jimps ofver the small lazy dog",
                "the quick brown fox jumps over the small lazy dog"))

    }

    @Test
    fun testExtractTop() {

        val res = FuzzySearch.extractTop("goolge", choices, 2)
        val res2 = FuzzySearch.extractTop("goolge", choices, SimpleRatio(), 2)

        assertEquals(res.size, 2)
        assertTrue(res.get(0).string == "google" && res.get(1).string == "googleplus")

        assertEquals(res2.size, 2)
        assertTrue(res2.get(0).string == "google" && res2.get(1).string == "googleplus")

        assertTrue(FuzzySearch.extractTop("goolge", choices, 2, 100).isEmpty())

    }

    @Test
    fun testExtractAll() {

        val res = FuzzySearch.extractAll("goolge", choices)

        assertEquals(res.size, choices.size)
        assertEquals(res.get(0).string, "google")

        assertEquals(FuzzySearch.extractAll("goolge", choices, 40).size, 3)

    }

    @Test
    fun testExtractSorted() {

        val res = FuzzySearch.extractSorted("goolge", choices)

        assertEquals(res.size, choices.size)
        assertEquals(res.get(0).string, "google")
        assertEquals(res.get(1).string, "googleplus")

        assertEquals(FuzzySearch.extractSorted("goolge", choices, 40).size, 3)

    }


    @Test
    fun testExtractOne() {

        val res = FuzzySearch.extractOne("twiter", choices, SimpleRatio())
        val res2 = FuzzySearch.extractOne("twiter", choices)
        val res3 = FuzzySearch.extractOne("cowboys", moreChoices)

        assertEquals(res.string, "twitter")
        assertEquals(res2.string, "twitter")
        assertTrue(res3.string == "Dallas Cowboys" && res3.score == 90)


    }


}


