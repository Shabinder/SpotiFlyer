package list

import com.shabinder.common.list.SpotiFlyerList
import extras.RenderableComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.css.*
import kotlinx.html.id
import react.RBuilder
import react.RState
import styled.css
import styled.styledDiv

class ListScreen(
    props: Props<SpotiFlyerList>,
) : RenderableComponent<SpotiFlyerList, ListScreen.State>(props,initialState = State(SpotiFlyerList.State())) {

    override val stateFlow: Flow<State> = model.models.map { State(it) }

    override fun RBuilder.render() {
        styledDiv {
            attrs {
                id = "list-screen-div"
            }
            css {
                classes = mutableListOf("list-screen")
                display = Display.flex
                flexDirection = FlexDirection.column
                flexGrow = 1.0
                justifyContent = JustifyContent.center
                alignItems = Align.center
                backgroundColor = Color.white
            }
        }
    }

    class State(
        var data: SpotiFlyerList.State
    ):RState
}