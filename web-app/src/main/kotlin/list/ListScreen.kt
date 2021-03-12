package list

import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.State
import extras.RenderableComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.css.*
import kotlinx.html.id
import react.RBuilder
import styled.css
import styled.styledDiv

class ListScreen(
    props: Props<SpotiFlyerList>,
) : RenderableComponent<SpotiFlyerList, State>(props,initialState = State()) {

    override val stateFlow: Flow<SpotiFlyerList.State> = model.models

    override fun RBuilder.render() {

        val result = state.data.queryResult

        styledDiv {
            attrs {
                id = "list-screen-div"
            }

            if(result == null){
                LoadingAnim {  }
            }else{
                CoverImage {
                    coverImageURL = result.coverUrl
                    coverName = result.title
                }

                DownloadAllButton {
                    isActive = state.data.trackList.isNotEmpty()
                }

                state.data.trackList.forEachIndexed{ index, trackDetails ->
                    TrackItem {
                        details = trackDetails
                        downloadTrack = model::onDownloadClicked
                    }
                }
            }

            css {
                classes = mutableListOf("list-screen")
                display = Display.flex
                flexDirection = FlexDirection.column
                flexGrow = 1.0
                justifyContent = JustifyContent.center
                alignItems = Align.stretch
            }
        }
    }
}