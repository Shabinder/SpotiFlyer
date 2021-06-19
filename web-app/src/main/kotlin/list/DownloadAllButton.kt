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

package list

import kotlinx.css.*
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledH5
import styled.styledImg

external interface DownloadAllButtonProps : RProps {
    var isActive:Boolean
    var link : String
    var downloadAll:()->Unit
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

    val (isClicked,setClicked) = useState(false)

    useEffect(mutableListOf(props.link)){
        setClicked(false)
    }

    if(props.isActive){
        if(isClicked) {
            styledDiv{
                css {
                    display = Display.flex
                    alignItems = Align.center
                    justifyContent = JustifyContent.center
                    height = 52.px
                }
                LoadingSpinner {  }
            }
        }
        else{
            styledDiv {
                attrs {
                    onClickFunction = {
                        props.downloadAll()
                        setClicked(true)
                    }
                }
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
                    display = Display.flex
                    alignItems = Align.center
                }
            }
        }
    }
}
