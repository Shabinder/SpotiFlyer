package com.willowtreeapps.fuzzywuzzy.diffutils.algorithms

import com.willowtreeapps.fuzzywuzzy.ToStringFunction

expect val pattern: String

class DefaultStringFunction : ToStringFunction<String> {

    /**
     * Performs the default string processing on the item string
     *
     * @param `item` Input string
     * @return The processed string
     */
    override fun apply(item: String) = subNonAlphaNumeric(item, " ").toLowerCase().trim { it <= ' ' }

    companion object {

        private const val nonUnicodePattern = "[^\\p{Alnum}]"
        private val r by lazy {
            try {
                Regex(pattern)
            } catch (e: IllegalArgumentException) {
                // Even though Android supports the unicode pattern class
                // for some reason it throws an IllegalArgumentException
                // if we pass the flag like on standard Java runtime
                //
                // We catch this and recompile without the flag (unicode should still work)
                Regex(nonUnicodePattern)
            }
        }


        /**
         * Substitute non alphanumeric characters.
         *
         * @param in The input string
         * @param sub The string to substitute with
         * @return The replaced string
         */
        fun subNonAlphaNumeric(`in`: String, sub: String): String {
            val m = r.find(`in`)
            return if (m != null) {
                r.replace(`in`, sub)
            } else {
                `in`
            }

        }
    }

}