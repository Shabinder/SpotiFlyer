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

package navbar

import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.filter
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.id
import kotlinx.html.js.onBlurFunction
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.attrs
import react.functionComponent
import styled.css
import styled.styledA
import styled.styledDiv
import styled.styledH1
import styled.styledImg
import styled.styledNav

@Suppress("FunctionName")
fun RBuilder.NavBar(handler: NavBarProps.() -> Unit) {
    return child(navBar) {
        attrs {
            handler()
        }
    }
}

external interface NavBarProps : RProps {
    var isBackVisible: Boolean
    var popBackToHomeScreen: () -> Unit
}


private val navBar = functionComponent<NavBarProps>("NavBar") { props ->

    styledNav {
        css {
            +NavBarStyles.nav
        }
        styledDiv {
            attrs {
                onClickFunction = {
                    props.popBackToHomeScreen()
                }
                onBlurFunction = {
                    props.popBackToHomeScreen()
                }
            }
            styledImg(src = "left-arrow.svg", alt = "Back Arrow") {
                css {
                    height = 42.px
                    width = 42.px
                    display = if (props.isBackVisible) Display.inline else Display.none
                    filter = "invert(100)"
                    marginRight = 12.px
                }
            }
        }
        styledA(href = "https://shabinder.github.io/SpotiFlyer/", target = "_blank") {
            css {
                display = Display.flex
                alignItems = Align.center
            }
            styledImg(src = "spotiflyer.svg", alt = "Logo") {
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
                css {
                    fontSize = 46.px
                    margin(horizontal = 14.px)
                }
            }
        }

        /*val (corsMode,setCorsMode) = useState(CorsProxy.SelfHostedCorsProxy() as CorsProxy)

        useEffect {
            setCorsMode(corsProxy)
        }*/

        styledDiv {

            /*styledH4 { + "Extension" }

            styledDiv {
                styledInput(type = InputType.checkBox) {
                    attrs{
                        id = "cmn-toggle-4"
                        value = "Extension"
                        checked = corsMode.extensionMode()
                        onChangeFunction = {
                            val state = it.target as HTMLInputElement
                            if(state.checked){
                                setCorsMode(corsProxy.toggle(CorsProxy.PublicProxyWithExtension()))
                            } else{
                                setCorsMode(corsProxy.toggle(CorsProxy.SelfHostedCorsProxy()))
                            }
                            println("Active Proxy:  ${corsProxy.url}")
                        }
                    }
                    css{
                        classes = mutableListOf("cmn-toggle","cmn-toggle-round-flat")
                    }
                }
                styledLabel { attrs { htmlFor = "cmn-toggle-4" } }
                css{
                    classes = mutableListOf("switch")
                    marginLeft = 8.px
                    marginRight = 16.px
                }
            }*/

            styledA(href = "https://github.com/Shabinder/SpotiFlyer/") {
                styledImg(src = "github.svg") {
                    css {
                        height = 42.px
                        width = 42.px
                    }
                }
            }
            css {
                display = Display.flex
                alignItems = Align.center
                marginLeft = LinearDimension.auto
            }
        }
    }
}