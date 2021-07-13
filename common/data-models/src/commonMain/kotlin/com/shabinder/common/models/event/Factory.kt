package com.shabinder.common.models.event

inline fun <V> runCatching(block: () -> V): Event<V, Throwable> {
    return try {
        Event.success(block())
    } catch (e: Throwable) {
        Event.error(e)
    }
}

inline infix fun <T, V> T.runCatching(block: T.() -> V): Event<V, Throwable> {
    return try {
        Event.success(block())
    } catch (e: Throwable) {
        Event.error(e)
    }
}
