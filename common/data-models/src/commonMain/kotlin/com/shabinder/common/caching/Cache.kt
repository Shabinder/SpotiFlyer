package com.shabinder.common.caching

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * An in-memory key-value cache with support for time-based (expiration) and size-based evictions.
 */
public interface Cache<in Key : Any, Value : Any> {

    /**
     * Returns the value associated with [key] in this cache, or null if there is no
     * cached value for [key].
     */
    public fun get(key: Key): Value?

    /**
     * Returns the value associated with [key] in this cache if exists,
     * otherwise gets the value by invoking [loader], associates the value with [key] in the cache,
     * and returns the cached value.
     *
     * Any exceptions thrown by the [loader] will be propagated to the caller of this function.
     */
    public suspend fun get(key: Key, loader: suspend () -> Value): Value

    public fun getBlocking(key: Key, loader: suspend () -> Value): Value

    /**
     * Associates [value] with [key] in this cache. If the cache previously contained a
     * value associated with [key], the old value is replaced by [value].
     */
    public fun put(key: Key, value: Value)

    /**
     * Discards any cached value for key [key].
     */
    public fun invalidate(key: Key)

    /**
     * Discards all entries in the cache.
     */
    public fun invalidateAll()

    /**
     * Returns a defensive copy of cache entries as [Map].
     */
    public fun asMap(): Map<in Key, Value>

    /**
     * Main entry point for creating a [Cache].
     */
    public interface Builder {

        /**
         * Specifies that each entry should be automatically removed from the cache once a fixed duration
         * has elapsed after the entry's creation or the most recent replacement of its value.
         *
         * When [duration] is zero, the cache's max size will be set to 0
         * meaning no values will be cached.
         */
        public fun expireAfterWrite(duration: Duration): Builder

        /**
         * Specifies that each entry should be automatically removed from the cache once a fixed duration
         * has elapsed after the entry's creation, the most recent replacement of its value, or its last
         * access.
         *
         * When [duration] is zero, the cache's max size will be set to 0
         * meaning no values will be cached.
         */
        public fun expireAfterAccess(duration: Duration): Builder

        /**
         * Specifies the maximum number of entries the cache may contain.
         * Cache eviction policy is based on LRU - i.e. least recently accessed entries get evicted first.
         *
         * When [size] is 0, entries will be discarded immediately and no values will be cached.
         *
         * If not set, cache size will be unlimited.
         */
        public fun maximumCacheSize(size: Long): Builder

        /**
         * Specifies a [FakeTimeSource] for programmatically advancing the reading of the underlying
         * [TimeSource] used for expiry checks in tests.
         *
         * If not specified, [TimeSource.Monotonic] will be used for expiry checks.
         */
        public fun fakeTimeSource(fakeTimeSource: FakeTimeSource): Builder

        /**
         * Builds a new instance of [Cache] with the specified configurations.
         */
        public fun <K : Any, V : Any> build(): Cache<K, V>

        public companion object {

            /**
             * Returns a new [Cache.Builder] instance.
             */
            public fun newBuilder(): Builder = CacheBuilderImpl()
        }
    }
}

/**
 * A default implementation of [Cache.Builder].
 */
internal class CacheBuilderImpl : Cache.Builder {

    private var expireAfterWriteDuration = Duration.INFINITE

    private var expireAfterAccessDuration = Duration.INFINITE
    private var maxSize = UNSET_LONG
    private var fakeTimeSource: FakeTimeSource? = null

    override fun expireAfterWrite(duration: Duration): CacheBuilderImpl = apply {
        require(duration.isPositive()) {
            "expireAfterWrite duration must be positive"
        }
        this.expireAfterWriteDuration = duration
    }

    override fun expireAfterAccess(duration: Duration): CacheBuilderImpl = apply {
        require(duration.isPositive()) {
            "expireAfterAccess duration must be positive"
        }
        this.expireAfterAccessDuration = duration
    }

    override fun maximumCacheSize(size: Long): CacheBuilderImpl = apply {
        require(size >= 0) {
            "maximum size must not be negative"
        }
        this.maxSize = size
    }

    override fun fakeTimeSource(fakeTimeSource: FakeTimeSource): CacheBuilderImpl = apply {
        this.fakeTimeSource = fakeTimeSource
    }

    override fun <K : Any, V : Any> build(): Cache<K, V> {
        return RealCache(
            expireAfterWriteDuration,
            expireAfterAccessDuration,
            maxSize,
            fakeTimeSource ?: TimeSource.Monotonic,
        )
    }

    companion object {
        internal const val UNSET_LONG: Long = -1
    }
}
