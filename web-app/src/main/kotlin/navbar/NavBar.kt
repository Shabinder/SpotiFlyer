package navbar

import kotlinx.css.*
import kotlinx.html.id
import react.*
import styled.*


fun RBuilder.navBar(attrs: RProps.() -> Unit): ReactElement{
    return child(NavBar::class){
        this.attrs(attrs)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class NavBar : RComponent<RProps, RState>() {

    override fun RBuilder.render() {
        styledNav {
            css {
                +NavBarStyles.nav
            }
            styledImg {
                attrs {
                    src = "spotiflyer.svg"
                }
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
}
