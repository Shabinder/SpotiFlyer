package com.shabinder.common.models.event.coroutines

class SuspendedValidation<out E : Throwable>(vararg resultSequence: SuspendableEvent<*, E>) {

    val failures: List<E> = resultSequence.filterIsInstance<SuspendableEvent.Failure<*, E>>().map { it.getThrowable() }

    val hasFailure = failures.isNotEmpty()
}
