package com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers

import androidx.lifecycle.LifecycleOwner
import com.shabinder.spotiflyer.utils.autoclear.LifecycleAutoInitializer

class LifecycleStartAndStopObserver<T : Any?>(
    private val initializer: (() -> T)?
) : LifecycleAutoInitializer<T> {

    override var value: T? = null

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        value = initializer?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        value = null
    }
}
