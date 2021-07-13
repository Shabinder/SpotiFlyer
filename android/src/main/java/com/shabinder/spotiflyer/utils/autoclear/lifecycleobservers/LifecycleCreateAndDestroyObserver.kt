package com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers

import androidx.lifecycle.LifecycleOwner
import com.shabinder.spotiflyer.utils.autoclear.LifecycleAutoInitializer

class LifecycleCreateAndDestroyObserver<T : Any?>(
    private val initializer: (() -> T)?
) : LifecycleAutoInitializer<T> {

    override var value: T? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        value = initializer?.invoke()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        value = null
    }
}
