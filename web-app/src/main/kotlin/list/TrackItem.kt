package list

import com.shabinder.common.models.TrackDetails
import kotlinx.css.*
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
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
            styledDiv {
                styledH3 {
                    + details.title
                    css {
                        padding(8.px)
                    }
                }
                css {
                    height = 40.px
                    display =Display.flex
                    alignItems = Align.center
                }
            }
            styledDiv {
                styledH4 {
                    + details.artists.joinToString(",")
                    css {
                        flexGrow = 1.0
                        padding(8.px)
                    }
                }
                styledH4 {
                    + "${details.durationSec} sec"
                    css {
                        flexGrow = 1.0
                        padding(8.px)
                        textAlign = TextAlign.right
                    }
                }
                css {
                    height = 40.px
                    display =Display.flex
                    alignItems = Align.center
                }
            }
            css {
                display = Display.flex
                flexGrow = 1.0
                flexDirection = FlexDirection.column
                margin(8.px)
            }
        }
        styledDiv {
            styledImg(src = "download-gradient.svg") {
                attrs {
                    onClickFunction = {
                        props.downloadTrack(details)
                    }
                }
                css {
                    margin(8.px)
                }
            }
            css {
                classes = mutableListOf("glow-button")
                borderRadius = 100.px
                width = 65.px
            }
        }

        css {
            alignItems = Align.center
            display =Display.flex
            flexDirection = FlexDirection.row
            flexGrow = 1.0
            color = Color.white
        }
    }
}
