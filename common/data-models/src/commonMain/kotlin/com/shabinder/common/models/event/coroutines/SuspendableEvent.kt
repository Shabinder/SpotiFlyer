@file:Suppress("UNCHECKED_CAST")

package com.shabinder.common.models.event.coroutines

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified X> SuspendableEvent<*, *>.getAs() = when (this) {
    is SuspendableEvent.Success -> value as? X
    is SuspendableEvent.Failure -> error as? X
}

suspend inline fun <V : Any?> SuspendableEvent<V, *>.success(noinline f: suspend (V) -> Unit) = fold(f, {})

suspend inline fun <E : Throwable> SuspendableEvent<*, E>.failure(noinline f: suspend (E) -> Unit) = fold({}, f)

infix fun <V : Any?, E : Throwable> SuspendableEvent<V, E>.or(fallback: V) = when (this) {
    is SuspendableEvent.Success -> this
    else -> SuspendableEvent.Success(fallback)
}

suspend inline infix fun <V : Any?, E : Throwable> SuspendableEvent<V, E>.getOrElse(crossinline fallback: suspend (E) -> V): V {
    return when (this) {
        is SuspendableEvent.Success -> value
        is SuspendableEvent.Failure -> fallback(error)
    }
}

fun <V : Any?, E : Throwable> SuspendableEvent<V, E>.getOrNull(): V? {
    return when (this) {
        is SuspendableEvent.Success -> value
        is SuspendableEvent.Failure -> null
    }
}

suspend inline fun <V : Any?, U : Any?, E : Throwable> SuspendableEvent<V, E>.map(
    crossinline transform: suspend (V) -> U
): SuspendableEvent<U, E> = try {
    when (this) {
        is SuspendableEvent.Success -> SuspendableEvent.Success(transform(value))
        is SuspendableEvent.Failure -> SuspendableEvent.Failure(error)
    }
} catch (ex: Throwable) {
    SuspendableEvent.error(ex as E)
}

suspend inline fun <V : Any?, U : Any?, E : Throwable> SuspendableEvent<V, E>.flatMap(
    crossinline transform: suspend (V) -> SuspendableEvent<U, E>
): SuspendableEvent<U, E> = try {
    when (this) {
        is SuspendableEvent.Success -> transform(value)
        is SuspendableEvent.Failure -> SuspendableEvent.Failure(error)
    }
} catch (ex: Throwable) {
    SuspendableEvent.error(ex as E)
}

suspend inline fun <V : Any?, E : Throwable, E2 : Throwable> SuspendableEvent<V, E>.mapError(
    crossinline transform: suspend (E) -> E2
) = try {
    when (this) {
        is SuspendableEvent.Success -> SuspendableEvent.Success<V, E2>(value)
        is SuspendableEvent.Failure -> SuspendableEvent.Failure<V, E2>(transform(error))
    }
} catch (ex: Throwable) {
    SuspendableEvent.error(ex as E)
}

suspend inline fun <V : Any?, E : Throwable, E2 : Throwable> SuspendableEvent<V, E>.flatMapError(
    crossinline transform: suspend (E) -> SuspendableEvent<V, E2>
) = try {
    when (this) {
        is SuspendableEvent.Success -> SuspendableEvent.Success(value)
        is SuspendableEvent.Failure -> transform(error)
    }
} catch (ex: Throwable) {
    SuspendableEvent.error(ex as E)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <V, E : Throwable> SuspendableEvent<V, E>.onSuccess(crossinline f: suspend (V) -> Unit): SuspendableEvent<V, E> {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    return fold({ f(it); this }, { this })
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <V, E : Throwable> SuspendableEvent<V, E>.onFailure(crossinline f: suspend (E) -> Unit): SuspendableEvent<V, E> {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    return fold({ this }, { f(it); this })
}

suspend inline fun <V : Any?, E : Throwable> SuspendableEvent<V, E>.any(
    crossinline predicate: suspend (V) -> Boolean
): Boolean = try {
    when (this) {
        is SuspendableEvent.Success -> predicate(value)
        is SuspendableEvent.Failure -> false
    }
} catch (ex: Throwable) {
    false
}

suspend inline fun <V : Any?, U : Any> SuspendableEvent<V, *>.fanout(
    crossinline other: suspend () -> SuspendableEvent<U, *>
): SuspendableEvent<Pair<V, U>, *> =
    flatMap { outer -> other().map { outer to it } }

suspend fun <V : Any?, E : Throwable> List<SuspendableEvent<V, E>>.lift(): SuspendableEvent<List<V>, E> = fold(
    SuspendableEvent.Success<MutableList<V>, E>(mutableListOf<V>()) as SuspendableEvent<MutableList<V>, E>
) { acc, result ->
    acc.flatMap { combine ->
        result.map { combine.apply { add(it) } }
    }
}

sealed class SuspendableEvent<out V : Any?, out E : Throwable> : ReadOnlyProperty<Any?, V> {

    abstract operator fun component1(): V?
    abstract operator fun component2(): E?

    suspend inline fun <X> fold(noinline success: suspend (V) -> X, noinline failure: suspend (E) -> X): X {
        return when (this) {
            is Success -> success(this.value)
            is Failure -> failure(this.error)
        }
    }

    abstract val value: V

    class Success<out V : Any?, out E : Throwable>(override val value: V) : SuspendableEvent<V, E>() {
        override fun component1(): V? = value
        override fun component2(): E? = null

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*, *> && value == other.value
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): V = value
    }

    class Failure<out V : Any?, out E : Throwable>(val error: E) : SuspendableEvent<V, E>() {
        override fun component1(): V? = null
        override fun component2(): E? = error

        override val value: V get() = throw error

        fun getThrowable(): E = error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*, *> && error == other.error
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): V = value
    }

    companion object {
        // Factory methods
        fun <E : Throwable> error(ex: E) = Failure<Nothing, E>(ex)

        fun <V : Any> success(res: V) = Success<V, Throwable>(res)

        inline fun <V : Any?> of(value: V?, crossinline fail: (() -> Throwable) = { Throwable() }): SuspendableEvent<V, Throwable> {
            return value?.let { Success<V, Nothing>(it) } ?: error(fail())
        }

        suspend inline fun <V : Any?, E : Throwable> of(
            crossinline block: suspend () -> V
        ): SuspendableEvent<V, E> = try {
            Success(block())
        } catch (ex: Throwable) {
            Failure(ex as E)
        }

        suspend inline operator fun <V : Any?> invoke(
            crossinline block: suspend () -> V
        ): SuspendableEvent<V, Throwable> = of(block)
    }
}
