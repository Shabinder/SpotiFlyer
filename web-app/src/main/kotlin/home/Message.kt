package home

import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledH1

external interface MessageProps : RProps {
    var text: String
}

@Suppress("FunctionName")
fun RBuilder.Message(handler:MessageProps.() -> Unit): ReactElement {
    return child(message){
        attrs {
            handler()
        }
    }
}

private val message = functionalComponent<MessageProps>("Message") { props->
    styledDiv {
        styledH1 {
            + props.text
            css {
                classes = mutableListOf("headingTitle")
                fontSize = 2.6.em
            }
        }
    }
}