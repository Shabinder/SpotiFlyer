package extras

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.ValueObserver
import extras.RenderableRootComponent.Props
import kotlinx.coroutines.*
import react.RComponent
import react.RProps
import react.RState
import react.setState

abstract class RenderableRootComponent<
        T : Any,
        S : RState
        >(
    props: Props<T>,
    initialState: S
) : RComponent<Props<T>, S>(props) {

    protected val model: T get() = props.model
    private val subscriptions = ArrayList<Subscription<*>>()
    protected var scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        this.state = initialState
    }

    override fun componentDidMount() {
        subscriptions.forEach { subscribe(it) }
        if(!scope.isActive)
            scope = CoroutineScope(Dispatchers.Default)
    }

    private fun <T : Any> subscribe(subscription: Subscription<T>) {
        subscription.value.subscribe(subscription.observer)
    }

    override fun componentWillUnmount() {
        subscriptions.forEach { unsubscribe(it) }
        scope.cancel("Component Unmounted")
    }

    private fun <T : Any> unsubscribe(subscription: Subscription<T>) {
        subscription.value.unsubscribe(subscription.observer)
    }

    protected fun <T : Any> Value<T>.bindToState(buildState: S.(T) -> Unit) {
        subscriptions += Subscription(this) { data -> setState { buildState(data) } }
    }

    interface Props<T : Any> : RProps {
        var model: T
    }

    protected class Subscription<T : Any>(
        val value: Value<T>,
        val observer: ValueObserver<T>
    )
}
