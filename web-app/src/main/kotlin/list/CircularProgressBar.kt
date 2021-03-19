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

import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.display
import kotlinx.css.justifyContent
import kotlinx.css.marginBottom
import kotlinx.css.px
import kotlinx.css.width
import react.RBuilder
import react.RProps
import react.ReactElement
import react.child
import react.functionalComponent
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