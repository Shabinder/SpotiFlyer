package extras

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import react.RComponent
import react.RProps
import react.RState
import react.setState

abstract class RenderableComponent<
        T : Any,
        S : Any
        >(
    props: Props<T>,
    initialState: S
) : RComponent<RenderableComponent.Props<T>, RenderableComponent.State<S>>(props) {

    protected abstract val stateFlow: Flow<S>
    protected val model: T get() = props.model
    protected var scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        state = State(data = initialState)
    }

    override fun componentDidMount() {
        if(!scope.isActive)
            scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            stateFlow.collect {
                setState { data = it }
            }
        }
    }

    override fun componentWillUnmount() {
        scope.cancel("Component Unmounted")
    }

    interface Props<T : Any> : RProps {
        var model: T
    }

    class State<S>(
        var data: S
    ):RState
}