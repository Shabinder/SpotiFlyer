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
import react.*
import styled.css
import styled.styledDiv
import styled.styledH1
import styled.styledImg


external interface CoverImageProps : RProps {
    var coverImageURL: String
    var coverName: String
}

@Suppress("FunctionName")
fun RBuilder.CoverImage(handler: CoverImageProps.() -> Unit): ReactElement {
    return child(coverImage){
        attrs {
            handler()
        }
    }
}

private val coverImage = functionalComponent<CoverImageProps>("CoverImage"){ props ->
    styledDiv {
        styledImg(src= props.coverImageURL){
            css {
                height = 220.px
                width = 220.px
            }
        }
        styledH1 {
            +props.coverName
            css {
                textAlign = TextAlign.center
            }
        }
        attrs {
            id = "cover-image"
        }
        css {
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.column
            marginTop = 12.px
        }
    }
}