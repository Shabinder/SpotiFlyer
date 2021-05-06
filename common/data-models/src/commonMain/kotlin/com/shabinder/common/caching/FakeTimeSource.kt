package com.shabinder.common.caching

import co.touchlab.stately.concurrency.AtomicLong
import kotlin.time.AbstractLongTimeSource
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * A time source that has programmatically updatable readings with support for multi-threaded access in Kotlin/Native.
 *
 * Implementation is identical to [kotlin.time.TestTimeSource] except the internal [reading] is an [AtomicLong].
 */
public class FakeTimeSource : AbstractLongTimeSource(unit = DurationUnit.NANOSECONDS) {

    private val reading = AtomicLong(0)

    override fun read(): Long = reading.get()

    /**
     * Advances the current reading value of this time source by the specified [duration].
     *
     * [duration] value is rounded down towards zero when converting it to a [Long] number of nanoseconds.
     * For example, if the duration being added is `0.6.nanoseconds`, the reading doesn't advance because
     * the duration value is rounded to zero nanoseconds.
     *
     * @throws IllegalStateException when the reading value overflows as the result of this operation.
     */
    public operator fun plusAssign(duration: Duration) {
        val delta = duration.toDouble(unit)
        val longDelta = delta.toLong()
        reading.set(
            reading.get().let { currentReading ->
                if (longDelta != Long.MIN_VALUE && longDelta != Long.MAX_VALUE) {
                    // when delta fits in long, add it as long
                    val newReading = currentReading + longDelta
                    if (currentReading xor longDelta >= 0 && currentReading xor newReading < 0) overflow(duration)
                    newReading
                } else {
                    // when delta is greater than long, add it as double
                    val newReading = currentReading + delta
                    if (newReading > Long.MAX_VALUE || newReading < Long.MIN_VALUE) overflow(duration)
                    newReading.toLong()
                }
            }
        )
    }

    private fun overflow(duration: Duration) {
        throw IllegalStateException("FakeTimeSource will overflow if its reading ${reading}ns is advanced by $duration.")
    }
}
