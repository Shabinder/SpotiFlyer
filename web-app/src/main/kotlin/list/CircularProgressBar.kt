package list

import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledSpan

@Suppress("FunctionName")
fun RBuilder.CircularProgressBar(handler: CircularProgressBarProps.() -> Unit): ReactElement {
    return child(circularProgressBar){
        attrs {
            handler()
        }
    }
}

external interface CircularProgressBarProps : RProps {
    var progress:Int
}

private val circularProgressBar = functionalComponent<CircularProgressBarProps>("Circular-Progress-Bar") { props->
    styledDiv {
        styledSpan { +"${props.progress}%" }
        styledDiv{
            css {
                classes = mutableListOf("left-half-clipper")
            }
            styledDiv{ css { classes = mutableListOf("first50-bar") } }
            styledDiv{ css { classes = mutableListOf("value-bar") } }
        }
        css{
            display = Display.flex
            justifyContent = JustifyContent.center
            classes = mutableListOf("progress-circle","p${props.progress}").apply { if(props.progress>50) add("over50") }
            width = 50.px
            marginBottom = 65.px
        }
    }
}