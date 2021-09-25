package com.shabinder.common.caching

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.collections.IsoMutableSet
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import com.shabinder.common.models.runBlocking
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A Kotlin Multiplatform [Cache] implementation powered by touchlab/Stately.
 *
 * Two types of evictions are supported:
 *
 * 1. Time-based evictions (expiration)
 * 2. Size-based evictions
 *
 * Time-based evictions are enabled by specifying [expireAfterWriteDuration] and/or [expireAfterAccessDuration].
 * When [expireAfterWriteDuration] is specified, entries will be automatically removed from the cache
 * once a fixed duration has elapsed after the entry's creation
 * or most recent replacement of its value.
 * When [expireAfterAccessDuration] is specified, entries will be automatically removed from the cache
 * once a fixed duration has elapsed after the entry's creation,
 * the most recent replacement of its value, or its last access.
 *
 * Note that creation and replacement of an entry is also considered an access.
 *
 * Size-based evictions are enabled by specifying [maxSize]. When the size of the cache entries grows
 * beyond [maxSize], least recently accessed entries will be evicted.
 */
internal class RealCache<Key : Any, Value : Any>(
    val expireAfterWriteDuration: Duration,
    val expireAfterAccessDuration: Duration,
    val maxSize: Long,
    val timeSource: TimeSource,
) : Cache<Key, Value> {

    private val cacheEntries = IsoMutableMap<Key, CacheEntry<Key, Value>>()

    /**
     * Whether to perform size based evictions.
     */
    private val evictsBySize = maxSize >= 0

    /**
     * Whether to perform write-time based expiration.
     */
    private val expiresAfterWrite = expireAfterWriteDuration.isFinite()

    /**
     * Whether to perform access-time (both read and write) based expiration.
     */
    private val expiresAfterAccess = expireAfterAccessDuration.isFinite()

    /**
     * A queue of unique cache entries ordered by write time.
     * Used for performing write-time based cache expiration.
     */
    private val writeQueue: IsoMutableSet<CacheEntry<Key, Value>>? =
        takeIf { expiresAfterWrite }?.let {
            ReorderingIsoMutableSet()
        }

    /**
     * A queue of unique cache entries ordered by access time.
     * Used for performing both write-time and read-time based cache expiration
     * as well as size-based eviction.
     *
     * Note that a write is also considered an access.
     */
    private val accessQueue: IsoMutableSet<CacheEntry<Key, Value>>? =
        takeIf { expiresAfterAccess || evictsBySize }?.let {
            ReorderingIsoMutableSet()
        }

    override fun get(key: Key): Value? {
        return cacheEntries[key]?.let {
            if (it.isExpired()) {
                // clean up expired entries and return null
                expireEntries()
                null
            } else {
                // update eviction metadata
                recordRead(it)
                it.value.get()
            }
        }
    }

    override suspend fun get(key: Key, loader: suspend () -> Value): Value {
        return cacheEntries[key]?.let {
            if (it.isExpired()) {
                // clean up expired entries
                expireEntries()
                null
            } else {
                // update eviction metadata
                recordRead(it)
                it.value.get()
            }
        } ?: loader().let { loadedValue ->
            val existingValue = get(key)
            if (existingValue != null) {
                existingValue
            } else {
                put(key, loadedValue)
                loadedValue
            }
        }
    }


    override fun getBlocking(key: Key, loader: suspend () -> Value): Value =
        runBlocking {
            get(key, loader)
        }

    override fun put(key: Key, value: Value) {
        expireEntries()

        val existingEntry = cacheEntries[key]
        if (existingEntry != null) {
            // cache entry found
            recordWrite(existingEntry)
            existingEntry.value.set(value)
        } else {
            // create a new cache entry
            val nowTimeMark = timeSource.markNow()
            val newEntry = CacheEntry(
                key = key,
                value = AtomicReference(value),
                accessTimeMark = AtomicReference(nowTimeMark),
                writeTimeMark = AtomicReference(nowTimeMark),
            )
            recordWrite(newEntry)
            cacheEntries[key] = newEntry
        }

        evictEntries()
    }

    override fun invalidate(key: Key) {
        expireEntries()
        cacheEntries.remove(key)?.also {
            writeQueue?.remove(it)
            accessQueue?.remove(it)
        }
    }

    override fun invalidateAll() {
        cacheEntries.clear()
        writeQueue?.clear()
        accessQueue?.clear()
    }

    override fun asMap(): Map<in Key, Value> {
        return cacheEntries.values.associate { entry ->
            entry.key to entry.value.get()
        }
    }

    /**
     * Remove all expired entries.
     */
    private fun expireEntries() {
        val queuesToProcess = listOfNotNull(
            if (expiresAfterWrite) writeQueue else null,
            if (expiresAfterAccess) accessQueue else null
        )

        queuesToProcess.forEach { queue ->
            queue.access {
                val iterator = queue.iterator()
                for (entry in iterator) {
                    if (entry.isExpired()) {
                        cacheEntries.remove(entry.key)
                        // remove the entry from the current queue
                        iterator.remove()
                    } else {
                        // found unexpired entry, no need to look any further
                        break
                    }
                }
            }
        }
    }

    /**
     * Check whether the [CacheEntry] has expired based on either access time or write time.
     */
    private fun CacheEntry<Key, Value>.isExpired(): Boolean {
        return expiresAfterAccess && (accessTimeMark.get() + expireAfterAccessDuration).hasPassedNow() ||
                expiresAfterWrite && (writeTimeMark.get() + expireAfterWriteDuration).hasPassedNow()
    }

    /**
     * Evict least recently accessed entries until [cacheEntries] is no longer over capacity.
     */
    private fun evictEntries() {
        if (!evictsBySize) {
            return
        }

        checkNotNull(accessQueue)

        while (cacheEntries.size > maxSize) {
            accessQueue.access {
                it.firstOrNull()?.run {
                    cacheEntries.remove(key)
                    writeQueue?.remove(this)
                    accessQueue.remove(this)
                }
            }
        }
    }

    /**
     * Update the eviction metadata on the [cacheEntry] which has just been read.
     */
    private fun recordRead(cacheEntry: CacheEntry<Key, Value>) {
        if (expiresAfterAccess) {
            val accessTimeMark = cacheEntry.accessTimeMark.value
            cacheEntry.accessTimeMark.set(accessTimeMark + accessTimeMark.elapsedNow())
        }
        accessQueue?.add(cacheEntry)
    }

    /**
     * Update the eviction metadata on the [CacheEntry] which is about to be written.
     * Note that a write is also considered an access.
     */
    private fun recordWrite(cacheEntry: CacheEntry<Key, Value>) {
        if (expiresAfterAccess) {
            val accessTimeMark = cacheEntry.accessTimeMark.value
            cacheEntry.accessTimeMark.set(accessTimeMark + accessTimeMark.elapsedNow())
        }
        if (expiresAfterWrite) {
            val writeTimeMark = cacheEntry.writeTimeMark.value
            cacheEntry.writeTimeMark.set(writeTimeMark + writeTimeMark.elapsedNow())
        }
        accessQueue?.add(cacheEntry)
        writeQueue?.add(cacheEntry)
    }
}

/**
 * A cache entry holds the [key] and [value] pair,
 * along with the metadata needed to perform cache expiration and eviction.
 */
private class CacheEntry<Key : Any, Value : Any>(
    val key: Key,
    val value: AtomicReference<Value>,
    val accessTimeMark: AtomicReference<TimeMark>,
    val writeTimeMark: AtomicReference<TimeMark>,
)
