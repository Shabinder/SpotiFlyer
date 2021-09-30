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

import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.WhiteSpace
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.px
import kotlinx.css.whiteSpace
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import react.PropsWithChildren
import react.RBuilder
import react.dom.attrs
import react.functionComponent
import react.useEffect
import react.useState
import styled.css
import styled.styledDiv
import styled.styledH5
import styled.styledImg

external interface DownloadAllButtonProps : PropsWithChildren {
    var isActive: Boolean
    var link: String
    var downloadAll: () -> Unit
}

@Suppress("FunctionName")
fun RBuilder.DownloadAllButton(handler: DownloadAllButtonProps.() -> Unit) {
    return child(downloadAllButton) {
        attrs {
            handler()
        }
    }
}

private val downloadAllButton = functionComponent<DownloadAllButtonProps>("DownloadAllButton") { props ->

    val (isClicked, setClicked) = useState(false)

    useEffect(mutableListOf(props.link)) {
        setClicked(false)
    }

    if (props.isActive) {
        if (isClicked) {
            styledDiv {
                css {
                    display = Display.flex
                    alignItems = Align.center
                    justifyContent = JustifyContent.center
                    height = 52.px
                }
                LoadingSpinner { }
            }
        } else {
            styledDiv {
                attrs {
                    onClickFunction = {
                        props.downloadAll()
                        setClicked(true)
                    }
                }
                styledDiv {

                    styledImg(src = "download.svg", alt = "Download All Button") {
                        css {
                            classes.add("download-all-icon")
                            height = 32.px
                        }
                    }

                    styledH5 {
                        attrs {
                            id = "download-all-text"
                        }
                        +"Download All"
                        css {
                            whiteSpace = WhiteSpace.nowrap
                            fontSize = 15.px
                        }
                    }

                    css {
                        classes.add("download-icon")
                        display = Display.flex
                        alignItems = Align.center
                    }
                }
                css {
                    classes.add("download-button")
                    display = Display.flex
                    alignItems = Align.center
                }
            }
        }
    }
}
