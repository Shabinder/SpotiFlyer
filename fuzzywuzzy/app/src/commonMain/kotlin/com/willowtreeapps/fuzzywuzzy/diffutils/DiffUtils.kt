package com.willowtreeapps.fuzzywuzzy.diffutils

import com.willowtreeapps.fuzzywuzzy.diffutils.structs.EditOp
import com.willowtreeapps.fuzzywuzzy.diffutils.structs.EditType
import com.willowtreeapps.fuzzywuzzy.diffutils.structs.EditType.*
import com.willowtreeapps.fuzzywuzzy.diffutils.structs.MatchingBlock
import com.willowtreeapps.fuzzywuzzy.diffutils.structs.OpCode

/**
 * This is a port of all the functions needed from python-levenshtein C implementation.
 * The code was ported line by line but unfortunately it was mostly undocumented,
 * so it is mostly non readable (eg. var names)
 */
object DiffUtils {

    private fun getEditOps(s1: String, s2: String): Array<EditOp> {
        return getEditOps(s1.length, s1, s2.length, s2)
    }


    private fun getEditOps(len1: Int, s1: String, len2: Int, s2: String): Array<EditOp> {
        var len1Copy = len1
        var len2Copy = len2

        var len1o = 0
        val len2o: Int
        var i = 0

        val matrix: IntArray

        val c1 = s1
        val c2 = s2

        var p1 = 0
        var p2 = 0

        while (len1Copy > 0 && len2Copy > 0 && c1[p1] == c2[p2]) {
            len1Copy--
            len2Copy--

            p1++
            p2++

            len1o++
        }

        len2o = len1o

        /* strip common suffix */
        while (len1Copy > 0 && len2Copy > 0 && c1[p1 + len1Copy - 1] == c2[p2 + len2Copy - 1]) {
            len1Copy--
            len2Copy--
        }

        len1Copy++
        len2Copy++

        matrix = IntArray(len2Copy * len1Copy)

        while (i < len2Copy) {
            matrix[i] = i
            i++
        }
        i = 1
        while (i < len1Copy) {
            matrix[len2Copy * i] = i
            i++
        }

        i = 1
        while (i < len1Copy) {

            var ptrPrev = (i - 1) * len2Copy
            var ptrC = i * len2Copy
            val ptrEnd = ptrC + len2Copy - 1

            val char1 = c1[p1 + i - 1]
            var ptrChar2 = p2

            var x = i

            ptrC++

            while (ptrC <= ptrEnd) {

                var c3 = matrix[ptrPrev++] + if (char1 != c2[ptrChar2++]) 1 else 0
                x++

                if (x > c3) {
                    x = c3
                }

                c3 = matrix[ptrPrev] + 1

                if (x > c3) {
                    x = c3
                }

                matrix[ptrC++] = x

            }
            i++

        }


        return editOpsFromCostMatrix(len1Copy, c1, p1, len1o, len2Copy, c2, p2, len2o, matrix)
    }


    private fun editOpsFromCostMatrix(len1: Int, c1: String, p1: Int, o1: Int,
                                      len2: Int, c2: String, p2: Int, o2: Int,
                                      matrix: IntArray): Array<EditOp> {

        var i: Int = len1 - 1
        var j: Int = len2 - 1
        var pos: Int = matrix[len1 * len2 - 1]

        var ptr: Int = len1 * len2 - 1

        val ops: Array<EditOp?>

        var dir = 0

        ops = arrayOfNulls(pos)

        while (i > 0 || j > 0) {

            if (dir < 0 && j != 0 && matrix[ptr] == matrix[ptr - 1] + 1) {

                val eop = EditOp()

                pos--
                ops[pos] = eop
                eop.type = INSERT
                eop.spos = i + o1
                eop.dpos = --j + o2
                ptr--

                continue
            }

            if (dir > 0 && i != 0 && matrix[ptr] == matrix[ptr - len2] + 1) {

                val eop = EditOp()

                pos--
                ops[pos] = eop
                eop.type = DELETE
                eop.spos = --i + o1
                eop.dpos = j + o2
                ptr -= len2

                continue

            }

            if (i != 0 && j != 0 && matrix[ptr] == matrix[ptr - len2 - 1]
                    && c1[p1 + i - 1] == c2[p2 + j - 1]) {

                i--
                j--
                ptr -= len2 + 1
                dir = 0

                continue

            }

            if (i != 0 && j != 0 && matrix[ptr] == matrix[ptr - len2 - 1] + 1) {

                pos--

                val eop = EditOp()
                ops[pos] = eop

                eop.type = REPLACE
                eop.spos = --i + o1
                eop.dpos = --j + o2

                ptr -= len2 + 1
                dir = 0
                continue

            }

            if (dir == 0 && j != 0 && matrix[ptr] == matrix[ptr - 1] + 1) {

                pos--
                val eop = EditOp()
                ops[pos] = eop
                eop.type = INSERT
                eop.spos = i + o1
                eop.dpos = --j + o2
                ptr--
                dir = -1

                continue
            }

            if (dir == 0 && i != 0 && matrix[ptr] == matrix[ptr - len2] + 1) {
                pos--
                val eop = EditOp()
                ops[pos] = eop

                eop.type = DELETE
                eop.spos = --i + o1
                eop.dpos = j + o2
                ptr -= len2
                dir = 1
                continue
            }

            assert(false)

        }

        return ops.requireNoNulls()

    }

    fun getMatchingBlocks(s1: String, s2: String): Array<MatchingBlock> {

        return getMatchingBlocks(s1.length, s2.length, getEditOps(s1, s2))

    }

    fun getMatchingBlocks(len1: Int, len2: Int, ops: Array<OpCode>): Array<MatchingBlock?> {

        val n = ops.size

        var noOfMB = 0
        var i: Int
        var o = 0

        i = n
        while (i-- != 0) {

            if (ops[o].type === KEEP) {

                noOfMB++

                while (i != 0 && ops[o].type === KEEP) {
                    i--
                    o++
                }

                if (i == 0)
                    break

            }
            o++

        }

        val matchingBlocks = arrayOfNulls<MatchingBlock>(noOfMB + 1)
        var mb = 0
        o = 0
        matchingBlocks[mb] = MatchingBlock()

        i = n
        while (i != 0) {

            if (ops[o].type === KEEP) {


                matchingBlocks[mb]!!.spos = ops[o].sbeg
                matchingBlocks[mb]!!.dpos = ops[o].dbeg

                while (i != 0 && ops[o].type === KEEP) {
                    i--
                    o++
                }

                if (i == 0) {
                    matchingBlocks[mb]!!.length = len1 - matchingBlocks[mb]!!.spos
                    mb++
                    break
                }

                matchingBlocks[mb]!!.length = ops[o].sbeg - matchingBlocks[mb]!!.spos
                mb++
                matchingBlocks[mb] = MatchingBlock()
            }
            i--
            o++


        }

        assert(mb == noOfMB)

        val finalBlock = MatchingBlock()
        finalBlock.spos = len1
        finalBlock.dpos = len2
        finalBlock.length = 0

        matchingBlocks[mb] = finalBlock

        return matchingBlocks


    }


    private fun getMatchingBlocks(len1: Int, len2: Int, ops: Array<EditOp>): Array<MatchingBlock> {

        val n = ops.size

        var numberOfMatchingBlocks = 0
        var i: Int
        var spos: Int
        var dpos: Int

        var o = 0

        dpos = 0
        spos = dpos

        var type: EditType

        i = n
        while (i != 0) {


            while (ops[o].type === KEEP && --i != 0) {
                o++
            }

            if (i == 0)
                break

            if (spos < ops[o].spos || dpos < ops[o].dpos) {

                numberOfMatchingBlocks++
                spos = ops[o].spos
                dpos = ops[o].dpos

            }

            type = ops[o].type!!

            when (type) {
                REPLACE -> do {
                    spos++
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                DELETE -> do {
                    spos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                INSERT -> do {
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                else -> {
                }
            }
        }

        if (spos < len1 || dpos < len2) {
            numberOfMatchingBlocks++
        }

        val matchingBlocks = arrayOfNulls<MatchingBlock>(numberOfMatchingBlocks + 1)

        o = 0
        dpos = 0
        spos = dpos
        var mbIndex = 0


        i = n
        while (i != 0) {

            while (ops[o].type === KEEP && --i != 0)
                o++

            if (i == 0)
                break

            if (spos < ops[o].spos || dpos < ops[o].dpos) {
                val mb = MatchingBlock()

                mb.spos = spos
                mb.dpos = dpos
                mb.length = ops[o].spos - spos
                spos = ops[o].spos
                dpos = ops[o].dpos

                matchingBlocks[mbIndex++] = mb

            }

            type = ops[o].type!!

            when (type) {
                REPLACE -> do {
                    spos++
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                DELETE -> do {
                    spos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                INSERT -> do {
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                else -> {
                }
            }
        }

        if (spos < len1 || dpos < len2) {
            assert(len1 - spos == len2 - dpos)

            val mb = MatchingBlock()
            mb.spos = spos
            mb.dpos = dpos
            mb.length = len1 - spos

            matchingBlocks[mbIndex++] = mb
        }

        assert(numberOfMatchingBlocks == mbIndex)

        val finalBlock = MatchingBlock()
        finalBlock.spos = len1
        finalBlock.dpos = len2
        finalBlock.length = 0

        matchingBlocks[mbIndex] = finalBlock


        return matchingBlocks.filterNotNull().toTypedArray()
    }


    private fun editOpsToOpCodes(ops: Array<EditOp>, len1: Int, len2: Int): Array<OpCode?> {

        val n = ops.size
        var noOfBlocks = 0
        var i: Int
        var spos: Int
        var dpos: Int
        var o = 0
        var type: EditType

        dpos = 0
        spos = dpos

        i = n
        while (i != 0) {

            while (ops[o].type === KEEP && --i != 0) {
                o++
            }

            if (i == 0)
                break

            if (spos < ops[o].spos || dpos < ops[o].dpos) {

                noOfBlocks++
                spos = ops[o].spos
                dpos = ops[o].dpos

            }

            // TODO: Is this right?
            noOfBlocks++
            type = ops[o].type!!

            when (type) {
                REPLACE -> do {
                    spos++
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                DELETE -> do {
                    spos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                INSERT -> do {
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                else -> {
                }
            }
        }

        if (spos < len1 || dpos < len2)
            noOfBlocks++

        val opCodes = arrayOfNulls<OpCode>(noOfBlocks)

        o = 0
        dpos = 0
        spos = dpos
        var oIndex = 0

        i = n
        while (i != 0) {

            while (ops[o].type === KEEP && --i != 0)
                o++

            if (i == 0)
                break

            val oc = OpCode()
            opCodes[oIndex] = oc
            oc.sbeg = spos
            oc.dbeg = dpos

            if (spos < ops[o].spos || dpos < ops[o].dpos) {

                oc.type = KEEP
                oc.send = ops[o].spos
                spos = oc.send
                oc.dend = ops[o].dpos
                dpos = oc.dend

                oIndex++
                val oc2 = OpCode()
                opCodes[oIndex] = oc2
                oc2.sbeg = spos
                oc2.dbeg = dpos

            }

            type = ops[o].type!!

            when (type) {
                REPLACE -> do {
                    spos++
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                DELETE -> do {
                    spos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                INSERT -> do {
                    dpos++
                    i--
                    o++
                } while (i != 0 && ops[o].type === type &&
                        spos == ops[o].spos && dpos == ops[o].dpos)

                else -> {
                }
            }

            opCodes[oIndex]!!.type = type
            opCodes[oIndex]!!.send = spos
            opCodes[oIndex]!!.dend = dpos
            oIndex++
        }

        if (spos < len1 || dpos < len2) {

            assert(len1 - spos == len2 - dpos)
            if (opCodes[oIndex] == null)
                opCodes[oIndex] = OpCode()
            opCodes[oIndex]!!.type = KEEP
            opCodes[oIndex]!!.sbeg = spos
            opCodes[oIndex]!!.dbeg = dpos
            opCodes[oIndex]!!.send = len1
            opCodes[oIndex]!!.dend = len2

            oIndex++

        }

        assert(oIndex == noOfBlocks)

        return opCodes

    }

    fun levEditDistance(s1: String, s2: String, xcost: Int): Int {

        var i: Int
        val half: Int

        var c1 = s1
        var c2 = s2

        var str1 = 0
        var str2 = 0

        var len1 = s1.length
        var len2 = s2.length

        /* strip common prefix */
        while (len1 > 0 && len2 > 0 && c1[str1] == c2[str2]) {

            len1--
            len2--
            str1++
            str2++

        }

        /* strip common suffix */
        while (len1 > 0 && len2 > 0 && c1[str1 + len1 - 1] == c2[str2 + len2 - 1]) {
            len1--
            len2--
        }

        /* catch trivial cases */
        if (len1 == 0)
            return len2
        if (len2 == 0)
            return len1

        /* make the inner cycle (i.e. str2) the longer one */
        if (len1 > len2) {

            val nx = len1
            val temp = str1

            len1 = len2
            len2 = nx

            str1 = str2
            str2 = temp

            val t = c2
            c2 = c1
            c1 = t

        }

        /* check len1 == 1 separately */
        if (len1 == 1) {
            return if (xcost != 0) {
                len2 + 1 - 2 * memchr(c2, str2, c1[str1], len2)
            } else {
                len2 - memchr(c2, str2, c1[str1], len2)
            }
        }

        len1++
        len2++
        half = len1 shr 1

        val row = IntArray(len2)
        var end = len2 - 1

        i = 0
        while (i < len2 - if (xcost != 0) 0 else half) {
            row[i] = i
            i++
        }


        /* go through the matrix and compute the costs.  yes, this is an extremely
         * obfuscated version, but also extremely memory-conservative and relatively
         * fast.  */

        if (xcost != 0) {

            i = 1
            while (i < len1) {

                var p = 1

                val ch1 = c1[str1 + i - 1]
                var c2p = str2

                var D = i
                var x = i

                while (p <= end) {

                    if (ch1 == c2[c2p++]) {
                        x = --D
                    } else {
                        x++
                    }
                    D = row[p]
                    D++

                    if (x > D)
                        x = D
                    row[p++] = x

                }
                i++

            }

        } else {

            /* in this case we don't have to scan two corner triangles (of size len1/2)
             * in the matrix because no best path can go throught them. note this
             * breaks when len1 == len2 == 2 so the memchr() special case above is
             * necessary */

            row[0] = len1 - half - 1
            i = 1
            while (i < len1) {
                var p: Int

                val ch1 = c1[str1 + i - 1]
                var c2p: Int

                var D: Int
                var x: Int

                /* skip the upper triangle */
                if (i >= len1 - half) {
                    val offset = i - (len1 - half)
                    val c3: Int

                    c2p = str2 + offset
                    p = offset
                    c3 = row[p++] + if (ch1 != c2[c2p++]) 1 else 0
                    x = row[p]
                    x++
                    D = x
                    if (x > c3) {
                        x = c3
                    }
                    row[p++] = x
                } else {
                    p = 1
                    c2p = str2
                    x = i
                    D = x
                }
                /* skip the lower triangle */
                if (i <= half + 1)
                    end = len2 + i - half - 2
                /* main */
                while (p <= end) {
                    val c3 = --D + if (ch1 != c2[c2p++]) 1 else 0
                    x++
                    if (x > c3) {
                        x = c3
                    }
                    D = row[p]
                    D++
                    if (x > D)
                        x = D
                    row[p++] = x

                }

                /* lower triangle sentinel */
                if (i <= half) {
                    val c3 = --D + if (ch1 != c2[c2p]) 1 else 0
                    x++
                    if (x > c3) {
                        x = c3
                    }
                    row[p] = x
                }
                i++
            }
        }

        i = row[end]

        return i

    }

    private fun memchr(haystack: String, offset: Int, needle: Char, num: Int): Int {
        var numCopy = num

        if (numCopy != 0) {
            var p = 0

            do {

                if (haystack[offset + p] == needle)
                    return 1

                p++

            } while (--numCopy != 0)

        }
        return 0

    }


    fun getRatio(s1: String, s2: String): Double {

        val len1 = s1.length
        val len2 = s2.length
        val lensum = len1 + len2

        val editDistance = levEditDistance(s1, s2, 1)

        return (lensum - editDistance) / lensum.toDouble()

    }


}

fun assert(assertion: Boolean) {
    if (!assertion)
        throw AssertionError()
}
