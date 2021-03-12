package navbar

import kotlinx.css.*
import kotlinx.html.id
import react.*
import styled.*


@Suppress("FunctionName")
fun RBuilder.NavBar(handler: NavBarProps.() -> Unit): ReactElement{
    return child(navBar){
        attrs {
            handler()
        }
    }
}

external interface NavBarProps:RProps{
    var isBackVisible: Boolean
}


private val navBar = functionalComponent<NavBarProps>("NavBar") { props ->
    styledNav {
        css {
            +NavBarStyles.nav
        }
        styledImg(src = "left-arrow.svg",alt = "Back Arrow"){
            css {
                height = 42.px
                width = 42.px
                display = if(props.isBackVisible) Display.inline else Display.none
                filter = "invert(100)"
                marginRight = 12.px
            }
        }
        styledImg(src = "spotiflyer.svg",alt = "Logo") {
            css {
                height = 42.px
                width = 42.px
            }
        }
        styledH1 {
            +"SpotiFlyer"
            attrs {
                id = "appName"
            }
            css{
                fontSize = 46.px
                margin(horizontal = 14.px)
            }
        }
        styledA(href = "https://github.com/Shabinder/SpotiFlyer/"){
            styledImg(src = "github.svg"){
                css {
                    height = 42.px
                    width = 42.px
                }
            }
            css {
                marginLeft = LinearDimension.auto
            }
        }
    }
}