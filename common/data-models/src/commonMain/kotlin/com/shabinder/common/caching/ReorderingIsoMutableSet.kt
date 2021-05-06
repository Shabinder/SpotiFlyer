package com.shabinder.common.caching

import co.touchlab.stately.collections.IsoMutableSet

/**
 * A custom [IsoMutableSet] that updates the insertion order when an element is re-inserted,
 * i.e. an inserted element will always be placed at the end
 * regardless of whether the element already exists.
 */
internal class ReorderingIsoMutableSet<T> : IsoMutableSet<T>(), MutableSet<T> {
    override fun add(element: T): Boolean = access {
        val exists = remove(element)
        super.add(element)
        // respect the contract "true if this set did not already contain the specified element"
        !exists
    }
}
