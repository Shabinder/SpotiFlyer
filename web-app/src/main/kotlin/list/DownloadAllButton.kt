package list

import kotlinx.css.*
import kotlinx.html.id
import react.*
import styled.css
import styled.styledDiv
import styled.styledH5
import styled.styledImg

external interface DownloadAllButtonProps : RProps {
    var isActive:Boolean
}

@Suppress("FunctionName")
fun RBuilder.DownloadAllButton(handler: DownloadAllButtonProps.() -> Unit): ReactElement {
    return child(downloadAllButton){
        attrs {
            handler()
        }
    }
}

private val downloadAllButton = functionalComponent<DownloadAllButtonProps>("DownloadAllButton") { props->
    styledDiv {
        styledDiv {

            styledImg(src = "download.svg",alt = "Download All Button") {
                css {
                    classes = mutableListOf("download-all-icon")
                    height = 32.px
                }
            }

            styledH5 {
                attrs {
                    id = "download-all-text"
                }
                + "Download All"
                css {
                    whiteSpace = WhiteSpace.nowrap
                    fontSize = 15.px
                }
            }

            css {
                classes = mutableListOf("download-icon")
                display = Display.flex
                alignItems = Align.center
            }
        }
        css {
            classes = mutableListOf("download-button")
            display = if(props.isActive) Display.flex else Display.none
            alignItems = Align.center
        }
    }
}
