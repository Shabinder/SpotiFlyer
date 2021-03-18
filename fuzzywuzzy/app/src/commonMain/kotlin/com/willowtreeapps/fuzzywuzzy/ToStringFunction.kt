/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
