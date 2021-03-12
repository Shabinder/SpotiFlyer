package root

import com.arkivanov.decompose.RouterState
import com.shabinder.common.root.SpotiFlyerRoot
import com.shabinder.common.root.SpotiFlyerRoot.*
import extras.RenderableRootComponent
import extras.renderableChild
import home.HomeScreen
import kotlinx.coroutines.launch
import list.ListScreen
import navbar.NavBar
import react.RBuilder
import react.RState

class RootR(props: Props<SpotiFlyerRoot>) : RenderableRootComponent<SpotiFlyerRoot, RootR.State>(
    props = props,
    initialState = State(routerState = props.model.routerState.value)
) {
    private val component: Child
        get() = model.routerState.value.activeChild.component

    override fun RBuilder.render() {
        NavBar {
            isBackVisible = (component is Child.List)
        }
        when(component){
            is Child.Main -> renderableChild(HomeScreen::class, (component as Child.Main).component)
            is Child.List -> renderableChild(ListScreen::class, (component as Child.List).component)
        }
    }

    init {
        model.routerState.bindToState { routerState = it }
    }
    class State(
        var routerState: RouterState<*, Child>,
    ) : RState

}
