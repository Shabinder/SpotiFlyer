package com.willowtreeapps.fuzzywuzzy.algorithms

import com.willowtreeapps.fuzzywuzzy.diffutils.algorithms.DefaultStringFunction
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultStringProcessorTest {

    @Ignore
    @Test
    fun testProcess() {
        val inp = "s.trim μεγιουνικουντ n/o/n a.lph.a n.um"

        assertEquals("s trim μεγιουνικουντ n o n a lph a n um", DefaultStringFunction().apply(inp))
    }
}