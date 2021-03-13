package list

import com.shabinder.common.models.DownloadStatus
import com.shabinder.common.models.TrackDetails
import kotlinx.css.*
import kotlinx.html.id
import react.*
import styled.*

external interface TrackItemProps : RProps {
    var details:TrackDetails
    var downloadTrack:(TrackDetails)->Unit
}

@Suppress("FunctionName")
fun RBuilder.TrackItem(handler: TrackItemProps.() -> Unit): ReactElement {
    return child(trackItem){
        attrs {
            handler()
        }
    }
}

private val trackItem = functionalComponent<TrackItemProps>("Track-Item"){ props ->
    val details = props.details
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
            styledDiv{
                css {
                    height = 40.px
                    alignItems = Align.center
                    display = Display.flex
                }
                styledH3 {
                    + details.title
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
                    + details.artists.joinToString(",")
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
                    + details.durationSec.toString()
                }
            }
        }
        when(details.downloaded){
            is DownloadStatus.NotDownloaded ->{
                DownloadButton {
                    onClick = { props.downloadTrack(details) }
                    status = details.downloaded
                }
            }
            is DownloadStatus.Downloading -> {
                CircularProgressBar {
                    progress = (details.downloaded as DownloadStatus.Downloading).progress
                }
            }
            DownloadStatus.Queued -> {
                LoadingSpinner {}
            }
            DownloadStatus.Downloaded -> {
                DownloadButton {
                    onClick = {}
                    status = details.downloaded
                }
            }
            DownloadStatus.Converting -> {
                LoadingSpinner {}
            }
            DownloadStatus.Failed -> {
                DownloadButton {
                    onClick = {}
                    status = details.downloaded
                }
            }
        }

        css {
            alignItems = Align.center
            display =Display.flex
            flexGrow = 1.0
        }
    }
}
