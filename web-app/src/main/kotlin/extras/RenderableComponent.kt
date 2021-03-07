package extras

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import react.RState
import react.setState

abstract class RenderableComponent<
        T : Any,
        S : RState
        >(
    props: Props<T>,
    initialState: S
) : RenderableRootComponent<T, S>(props,initialState) {

    protected abstract val stateFlow: Flow<S>

    override fun componentDidMount() {
        super.componentDidMount()
        scope.launch {
            stateFlow.collectLatest {
                setState { state = it }
            }
        }
    }
}