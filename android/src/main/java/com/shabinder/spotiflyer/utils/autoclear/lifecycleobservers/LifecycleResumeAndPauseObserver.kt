package com.shabinder.spotiflyer.utils.autoclear.lifecycleobservers

import androidx.lifecycle.LifecycleOwner
import com.shabinder.spotiflyer.utils.autoclear.LifecycleAutoInitializer

class LifecycleResumeAndPauseObserver<T : Any?>(
    private val initializer: (() -> T)?
) : LifecycleAutoInitializer<T> {

    override var value: T? = null

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        value = initializer?.invoke()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        value = null
    }
}
