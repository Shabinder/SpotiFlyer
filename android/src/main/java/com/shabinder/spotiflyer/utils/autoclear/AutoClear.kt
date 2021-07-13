package com.shabinder.spotiflyer.utils.autoclear

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.shabinder.spotiflyer.utils.autoclear.AutoClear.Companion.TRIGGER
import com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers.LifecycleCreateAndDestroyObserver
import com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers.LifecycleResumeAndPauseObserver
import com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers.LifecycleStartAndStopObserver
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClear<T : Any?>(
    lifecycle: Lifecycle,
    private val initializer: (() -> T)?,
    private val trigger: TRIGGER = TRIGGER.ON_CREATE,
) : ReadWriteProperty<LifecycleOwner, T?> {

    companion object {
        enum class TRIGGER {
            ON_CREATE,
            ON_START,
            ON_RESUME
        }
    }

    private var _value: T?
        get() = observer.value
        set(value) { observer.value = value }

    val value: T get() = _value ?: initializer?.invoke()
        ?: throw IllegalStateException("The value has not yet been set or no default initializer provided")

    fun getOrNull(): T? = _value

    private val observer: LifecycleAutoInitializer<T?> by lazy {
        when (trigger) {
            TRIGGER.ON_CREATE -> LifecycleCreateAndDestroyObserver(initializer)
            TRIGGER.ON_START -> LifecycleStartAndStopObserver(initializer)
            TRIGGER.ON_RESUME -> LifecycleResumeAndPauseObserver(initializer)
        }
    }

    init {
        lifecycle.addObserver(observer)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {

        if (_value != null) {
            return value
        }

        // If for Some Reason Initializer is not invoked even after Initialisation, invoke it after checking state
        if (thisRef.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            return initializer?.invoke().also { _value = it }
                ?: throw IllegalStateException("The value has not yet been set or no default initializer provided")
        } else {
            throw IllegalStateException("Activity might have been destroyed or not initialized yet")
        }
    }

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T?) {
        this._value = value
    }

    fun reset() {
        this._value = null
    }
}

fun <T : Any> LifecycleOwner.autoClear(
    trigger: TRIGGER = TRIGGER.ON_CREATE,
    initializer: () -> T
): AutoClear<T> {
    return AutoClear(this.lifecycle, initializer, trigger)
}
