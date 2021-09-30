/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package list

import com.shabinder.common.list.SpotiFlyerList
import com.shabinder.common.list.SpotiFlyerList.State
import extras.Props
import extras.RStateWrapper
import extras.RenderableComponent
import kotlinx.css.*
import kotlinx.html.id
import react.RBuilder
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledSection


class ListScreen(
    props: Props<SpotiFlyerList>,
) : RenderableComponent<SpotiFlyerList, RStateWrapper<State>>(
    props,
    initialState = RStateWrapper(props.component.model.value)
) {
    init {
        component.model.bindToState {
            model = it
        }
    }

    override fun RBuilder.render() {

        val queryResult = state.model.queryResult

        styledSection {
            attrs {
                id = "list-screen"
            }

            if(queryResult == null) {
                LoadingAnim {  }
            }else {
                CoverImage {
                    coverImageURL = queryResult.coverUrl
                    coverName = queryResult.title
                }

                DownloadAllButton {
                    isActive = state.model.trackList.size > 1
                    downloadAll = {
                        component.onDownloadAllClicked(state.model.trackList)
                    }
                    link = state.model.link
                }

                styledDiv {
                    css {
                        display =Display.flex
                        flexGrow = 1.0
                        flexDirection = FlexDirection.column
                        color = Color.white
                    }
                    state.model.trackList.forEachIndexed{ _, trackDetails ->
                        TrackItem {
                            details = trackDetails
                            downloadTrack = component::onDownloadClicked
                        }
                    }
                }
            }

            css {
                classes.add("list-screen")
                display = Display.flex
                padding(8.px)
                flexDirection = FlexDirection.column
                flexGrow = 1.0
            }
        }
    }
}