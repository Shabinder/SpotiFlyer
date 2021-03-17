package list

import com.shabinder.common.models.DownloadStatus
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.*
import styled.css
import styled.styledDiv
import styled.styledImg

@Suppress("FunctionName")
fun RBuilder.DownloadButton(handler: DownloadButtonProps.() -> Unit): ReactElement {
    return child(downloadButton){
        attrs {
            handler()
        }
    }
}

external interface DownloadButtonProps : RProps {
    var onClick:()->Unit
    var status :DownloadStatus
}

private val downloadButton = functionalComponent<DownloadButtonProps>("Circular-Progress-Bar") { props->
    styledDiv {
        val src = when(props.status){
            is DownloadStatus.NotDownloaded -> "download-gradient.svg"
            is DownloadStatus.Downloaded -> "check.svg"
            is DownloadStatus.Failed -> "error.svg"
            else -> ""
        }
        styledImg(src = src) {
            attrs {
                onClickFunction = {
                    props.onClick()
                }
            }
            css {
                width = (2.5).em
                margin(8.px)
            }
        }
        css {
            classes = mutableListOf("glow-button")
            borderRadius = 100.px
        }
    }
}