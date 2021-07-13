package com.shabinder.spotiflyer.utils.autoclear

import androidx.lifecycle.DefaultLifecycleObserver

interface LifecycleAutoInitializer<T> : DefaultLifecycleObserver {
    var value: T?
}
