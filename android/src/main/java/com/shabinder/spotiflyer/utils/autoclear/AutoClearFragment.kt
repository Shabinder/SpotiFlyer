package com.shabinder.spotiflyer.utils.autoclear

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClearFragment<T : Any?>(
    fragment: Fragment,
    private val initializer: (() -> T)?
) : ReadWriteProperty<Fragment, T?> {

    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            val viewLifecycleOwnerObserver = Observer<LifecycleOwner?> { viewLifecycleOwner ->

                viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) {
                        _value = null
                    }
                })
            }

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerObserver)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerObserver)
            }
        }
        )
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val value = _value

        if (value != null) {
            return value
        }

        if (thisRef.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            return initializer?.invoke().also { _value = it }
                ?: throw IllegalStateException("The value has not yet been set or no default initializer provided")
        } else {
            throw IllegalStateException("Fragment might have been destroyed or not initialized yet")
        }
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        _value = value
    }
}

fun <T : Any> Fragment.autoClear(initializer: () -> T): AutoClearFragment<T> {
    return AutoClearFragment(this, initializer)
}
