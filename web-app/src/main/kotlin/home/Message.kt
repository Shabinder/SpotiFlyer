package home

import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledH1


data class MessageState(var link:String): RState

external interface MessageProps : RProps {
    var text: String
}

fun RBuilder.message(attrs:MessageProps.() -> Unit): ReactElement {
    return child(Message::class){
        this.attrs(attrs)
    }
}

class Message(props:MessageProps): RComponent<MessageProps,MessageState>(props) {
    override fun RBuilder.render() {
        styledDiv {
            styledH1 {
                +"Your Gateway to Nirvana, for FREE!"
                css {
                    classes = mutableListOf("headingTitle")
                    fontSize = 3.2.rem
                }
            }
        }
    }
}