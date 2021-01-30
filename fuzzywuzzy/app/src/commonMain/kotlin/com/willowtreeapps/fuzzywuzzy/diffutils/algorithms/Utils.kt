package com.willowtreeapps.fuzzywuzzy.diffutils.algorithms

import com.willowtreeapps.fuzzywuzzy.diffutils.PriorityQueue


object Utils {


    internal fun tokenize(`in`: String): List<String> {

        return listOf(*`in`.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    }

    internal fun tokenizeSet(`in`: String): Set<String> {

        return HashSet(tokenize(`in`))

    }

    internal fun sortAndJoin(col: List<String>, sep: String): String {

//        Collections.sort(col)

        return join(col.sorted(), sep)

    }

    internal fun join(strings: List<String>, sep: String): String {
        val buf = StringBuilder(strings.size * 16)

        for (i in strings.indices) {

            if (i < strings.size) {
                buf.append(sep)
            }

            buf.append(strings[i])

        }

        return buf.toString().trim { it <= ' ' }
    }

    internal fun sortAndJoin(col: Set<String>, sep: String): String {

        return sortAndJoin(ArrayList(col), sep)

    }

    fun <T : Comparable<T>> findTopKHeap(arr: List<T>, k: Int): List<T> {
        val pq = PriorityQueue<T>(arr.size)

        for (x in arr) {
            if (pq.size < k)
                pq.add(x)
            else if (x > pq.peek()) {
                pq.poll()
                pq.add(x)
            }
        }
        val res = ArrayList<T>()
        try {
            for (i in k downTo 1) {
                res.add(pq.poll())
            }
        } catch (e: NoSuchElementException) {

        }
        return res

    }

    internal fun <T : Comparable<T>> max(vararg elems: T): T? {

        if (elems.isEmpty()) return null

        var best = elems[0]

        for (t in elems) {
            if (t.compareTo(best) > 0) {
                best = t
            }
        }

        return best

    }


}