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
import styled.styledSection

class ListScreen(
    props: Props<SpotiFlyerList>,
) : RenderableComponent<SpotiFlyerList, State>(props,initialState = State()) {

    override val stateFlow: Flow<SpotiFlyerList.State> = model.models

    override fun RBuilder.render() {

        val result = state.data.queryResult

        styledSection {
            attrs {
                id = "list-screen"
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

                styledDiv{
                    css {
                        display =Display.flex
                        flexGrow = 1.0
                        flexDirection = FlexDirection.column
                        color = Color.white
                    }
                    state.data.trackList.forEachIndexed{ index, trackDetails ->
                        TrackItem {
                            details = trackDetails
                            downloadTrack = model::onDownloadClicked
                        }
                    }
                }
            }

            css {
                classes = mutableListOf("list-screen")
                display = Display.flex
                padding(8.px)
                flexDirection = FlexDirection.column
                flexGrow = 1.0
            }
        }
    }
}