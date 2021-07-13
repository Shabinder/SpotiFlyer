package com.shabinder.common.models.event

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified X> Event<*, *>.getAs() = when (this) {
    is Event.Success -> value as? X
    is Event.Failure -> error as? X
}

inline fun <V : Any?> Event<V, *>.success(f: (V) -> Unit) = fold(f, {})

inline fun <E : Throwable> Event<*, E>.failure(f: (E) -> Unit) = fold({}, f)

infix fun <V : Any?, E : Throwable> Event<V, E>.or(fallback: V) = when (this) {
    is Event.Success -> this
    else -> Event.Success(fallback)
}

inline infix fun <V : Any?, E : Throwable> Event<V, E>.getOrElse(fallback: (E) -> V): V {
    return when (this) {
        is Event.Success -> value
        is Event.Failure -> fallback(error)
    }
}

fun <V : Any?, E : Throwable> Event<V, E>.getOrNull(): V? {
    return when (this) {
        is Event.Success -> value
        is Event.Failure -> null
    }
}

fun <V : Any?, E : Throwable> Event<V, E>.getThrowableOrNull(): E? {
    return when (this) {
        is Event.Success -> null
        is Event.Failure -> error
    }
}

inline fun <V : Any?, E : Throwable, U : Any?, F : Throwable> Event<V, E>.mapEither(
    success: (V) -> U,
    failure: (E) -> F
): Event<U, F> {
    return when (this) {
        is Event.Success -> Event.success(success(value))
        is Event.Failure -> Event.error(failure(error))
    }
}

inline fun <V : Any?, U : Any?, reified E : Throwable> Event<V, E>.map(transform: (V) -> U): Event<U, E> = try {
    when (this) {
        is Event.Success -> Event.Success(transform(value))
        is Event.Failure -> Event.Failure(error)
    }
} catch (ex: Throwable) {
    when (ex) {
        is E -> Event.error(ex)
        else -> throw ex
    }
}

inline fun <V : Any?, U : Any?, reified E : Throwable> Event<V, E>.flatMap(transform: (V) -> Event<U, E>): Event<U, E> =
    try {
        when (this) {
            is Event.Success -> transform(value)
            is Event.Failure -> Event.Failure(error)
        }
    } catch (ex: Throwable) {
        when (ex) {
            is E -> Event.error(ex)
            else -> throw ex
        }
    }

inline fun <V : Any?, E : Throwable, E2 : Throwable> Event<V, E>.mapError(transform: (E) -> E2) = when (this) {
    is Event.Success -> Event.Success(value)
    is Event.Failure -> Event.Failure(transform(error))
}

inline fun <V : Any?, E : Throwable, E2 : Throwable> Event<V, E>.flatMapError(transform: (E) -> Event<V, E2>) =
    when (this) {
        is Event.Success -> Event.Success(value)
        is Event.Failure -> transform(error)
    }

inline fun <V : Any?, E : Throwable> Event<V, E>.onError(f: (E) -> Unit) = when (this) {
    is Event.Success -> Event.Success(value)
    is Event.Failure -> {
        f(error)
        this
    }
}

inline fun <V : Any?, E : Throwable> Event<V, E>.onSuccess(f: (V) -> Unit): Event<V, E> {
    return when (this) {
        is Event.Success -> {
            f(value)
            this
        }
        is Event.Failure -> this
    }
}

inline fun <V : Any?, E : Throwable> Event<V, E>.any(predicate: (V) -> Boolean): Boolean = try {
    when (this) {
        is Event.Success -> predicate(value)
        is Event.Failure -> false
    }
} catch (ex: Throwable) {
    false
}

inline fun <V : Any?, U : Any?> Event<V, *>.fanout(other: () -> Event<U, *>): Event<Pair<V, U>, *> =
    flatMap { outer -> other().map { outer to it } }

inline fun <V : Any?, reified E : Throwable> List<Event<V, E>>.lift(): Event<List<V>, E> = fold(
    Event.success(
        mutableListOf<V>()
    ) as Event<MutableList<V>, E>
) { acc, Event ->
    acc.flatMap { combine ->
        Event.map { combine.apply { add(it) } }
    }
}

inline fun <V, E : Throwable> Event<V, E>.unwrap(failure: (E) -> Nothing): V =
    apply { component2()?.let(failure) }.component1()!!

inline fun <V, E : Throwable> Event<V, E>.unwrapError(success: (V) -> Nothing): E =
    apply { component1()?.let(success) }.component2()!!

sealed class Event<out V : Any?, out E : Throwable> : ReadOnlyProperty<Any?, V> {

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    inline fun <X> fold(success: (V) -> X, failure: (E) -> X): X = when (this) {
        is Success -> success(this.value)
        is Failure -> failure(this.error)
    }

    abstract val value: V

    class Success<out V : Any?>(override val value: V) : Event<V, Nothing>() {
        override fun component1(): V? = value

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*> && value == other.value
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): V = value
    }

    class Failure<out E : Throwable>(val error: E) : Event<Nothing, E>() {
        override fun component2(): E = error

        override val value: Nothing get() = throw error

        fun getThrowable(): E = error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*> && error == other.error
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Nothing = value
    }

    companion object {
        // Factory methods
        fun <E : Throwable> error(ex: E) = Failure(ex)

        fun <V : Any?> success(v: V) = Success(v)

        inline fun <V : Any?> of(
            value: V?,
            fail: (() -> Throwable) = { Throwable() }
        ): Event<V, Throwable> =
            value?.let { success(it) } ?: error(fail())

        inline fun <V : Any?, reified E : Throwable> of(crossinline f: () -> V): Event<V, E> = try {
            success(f())
        } catch (ex: Throwable) {
            when (ex) {
                is E -> error(ex)
                else -> throw ex
            }
        }

        inline operator fun <V : Any?> invoke(crossinline f: () -> V): Event<V, Throwable> = try {
            success(f())
        } catch (ex: Throwable) {
            error(ex)
        }
    }
}
