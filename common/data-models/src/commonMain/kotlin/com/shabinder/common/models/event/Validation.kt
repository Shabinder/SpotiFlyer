package com.shabinder.common.models.event

class Validation<out E : Throwable>(vararg resultSequence: Event<*, E>) {

    val failures: List<E> = resultSequence.filterIsInstance<Event.Failure<E>>().map { it.getThrowable() }

    val hasFailure = failures.isNotEmpty()
}
