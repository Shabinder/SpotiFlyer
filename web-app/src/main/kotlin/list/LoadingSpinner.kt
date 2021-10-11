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

import kotlinx.css.marginRight
import kotlinx.css.px
import kotlinx.css.width
import react.PropsWithChildren
import react.RBuilder
import react.functionComponent
import styled.css
import styled.styledDiv

@Suppress("FunctionName")
fun RBuilder.LoadingSpinner(handler: PropsWithChildren.() -> Unit) {
    return child(loadingSpinner) {
        attrs {
            handler()
        }
    }
}

private val loadingSpinner = functionComponent<PropsWithChildren>("Loading-Spinner") {
    styledDiv {
        styledDiv {}
        styledDiv {}
        styledDiv {}
        styledDiv {}
        css {
            classes.add("lds-ring")
            width = 50.px
            marginRight = 8.px
        }
    }
}