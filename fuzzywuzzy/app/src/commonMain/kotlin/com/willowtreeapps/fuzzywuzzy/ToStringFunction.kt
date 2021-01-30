package com.willowtreeapps.fuzzywuzzy


/**
 * Transforms an item of type T to a String.
 *
 * @param <T> The type of the item to transform.
</T> */
interface ToStringFunction<T> {
    /**
     * Transforms the input item to a string.
     *
     * @param item The item to transform.
     * @return A string to use for comparing the item.
     */
    fun apply(item: T): String

    companion object {

        /**
         * A default ToStringFunction that returns the input string;
         * used by methods that use plain strings in [FuzzySearch].
         */
        val NO_PROCESS: ToStringFunction<String> = object : ToStringFunction<String> {
            override fun apply(item: String): String {
                return item
            }
        }
    }
}
