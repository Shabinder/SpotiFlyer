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

import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.Overflow
import kotlinx.css.TextAlign
import kotlinx.css.TextOverflow
import kotlinx.css.WhiteSpace
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.em
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.minWidth
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.paddingRight
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.textOverflow
import kotlinx.css.whiteSpace
import kotlinx.css.width
import kotlinx.html.id
import react.PropsWithChildren
import react.RBuilder
import react.dom.attrs
import react.functionComponent
import react.useEffect
import react.useState
import styled.css
import styled.styledDiv
import styled.styledH3
import styled.styledH4
import styled.styledImg

external interface TrackItemProps : PropsWithChildren {
    var details: TrackDetails
    var downloadTrack: (TrackDetails) -> Unit
}

@Suppress("FunctionName")
fun RBuilder.TrackItem(handler: TrackItemProps.() -> Unit) {
    return child(trackItem) {
        attrs {
            handler()
        }
    }
}

private val trackItem = functionComponent<TrackItemProps>("Track-Item") { props ->
    val (downloadStatus, setDownloadStatus) = useState(props.details.downloaded)
    val details = props.details
    useEffect(listOf(props.details)) {
        setDownloadStatus(props.details.downloaded)
    }
    styledDiv {
        styledImg(src = details.albumArtURL) {
            css {
                height = 90.px
                width = 90.px
            }
        }

        styledDiv {
            attrs {
                id = "text-details"
            }
            css {
                flexGrow = 1.0
                minWidth = 0.px
                display = Display.flex
                flexDirection = FlexDirection.column
                margin(8.px)
            }
            styledDiv {
                css {
                    height = 40.px
                    alignItems = Align.center
                    display = Display.flex
                }
                styledH3 {
                    +details.title
                    css {
                        padding(8.px)
                        fontSize = 1.3.em
                        textOverflow = TextOverflow.ellipsis
                        whiteSpace = WhiteSpace.nowrap
                        overflow = Overflow.hidden
                    }
                }
            }
            styledDiv {
                css {
                    height = 40.px
                    alignItems = Align.center
                    display = Display.flex
                }
                styledH4 {
                    +details.artists.joinToString(",")
                    css {
                        flexGrow = 1.0
                        padding(8.px)
                        minWidth = 4.em
                        fontSize = 1.1.em
                        textOverflow = TextOverflow.ellipsis
                        whiteSpace = WhiteSpace.nowrap
                        overflow = Overflow.hidden
                    }
                }
                styledH4 {
                    css {
                        textAlign = TextAlign.end
                        flexGrow = 1.0
                        padding(8.px)
                        minWidth = 4.em
                        fontSize = 1.1.em
                        textOverflow = TextOverflow.ellipsis
                        whiteSpace = WhiteSpace.nowrap
                        overflow = Overflow.hidden
                    }
                    +"${details.durationSec / 60} min, ${details.durationSec % 60} sec"
                }
            }
        }
        when (downloadStatus) {
            is DownloadStatus.NotDownloaded -> {
                DownloadButton {
                    onClick = {
                        setDownloadStatus(DownloadStatus.Queued)
                        props.downloadTrack(details)
                    }
                    status = downloadStatus
                }
            }
            //TODO Fix Progress Indicator
            /*is DownloadStatus.Downloading -> {
                CircularProgressBar {
                    progress = downloadStatus.progress
                }
            }
            is DownloadStatus.Converting -> {
                LoadingSpinner {}
            }
            is DownloadStatus.Queued -> {
                LoadingSpinner {}
            }*/
            is DownloadStatus.Downloaded -> {
                DownloadButton {
                    onClick = {}
                    status = downloadStatus
                }
            }
            is DownloadStatus.Failed -> {
                DownloadButton {
                    onClick = {}
                    status = downloadStatus
                }
            }
            else -> LoadingSpinner { }
        }

        css {
            alignItems = Align.center
            display = Display.flex
            paddingRight = 16.px
        }
    }
}
