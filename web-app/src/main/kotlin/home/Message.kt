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

package home

import kotlinx.css.em
import kotlinx.css.fontSize
import react.PropsWithChildren
import react.RBuilder
import react.functionComponent
import styled.css
import styled.styledDiv
import styled.styledH1

external interface MessageProps : PropsWithChildren {
    var text: String
}

@Suppress("FunctionName")
fun RBuilder.Message(handler: MessageProps.() -> Unit) {
    return child(message) {
        attrs {
            handler()
        }
    }
}

private val message = functionComponent<MessageProps>("Message") { props ->
    styledDiv {
        styledH1 {
            +props.text
            css {
                classes.add("headingTitle")
                fontSize = 2.6.em
            }
        }
    }
}