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
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.px
import kotlinx.css.width
import react.PropsWithChildren
import react.RBuilder
import react.functionComponent
import styled.css
import styled.styledDiv

@Suppress("FunctionName")
fun RBuilder.LoadingAnim(handler: PropsWithChildren.() -> Unit) {
    return child(loadingAnim) {
        attrs {
            handler()
        }
    }
}

private val loadingAnim = functionComponent<PropsWithChildren>("Loading Animation") {
    styledDiv {
        css {
            flexGrow = 1.0
            display = Display.flex
            alignItems = Align.center
        }
        styledDiv {
            styledDiv { css { classes.add("sk-cube sk-cube1") } }
            styledDiv { css { classes.add("sk-cube sk-cube2") } }
            styledDiv { css { classes.add("sk-cube sk-cube3") } }
            styledDiv { css { classes.add("sk-cube sk-cube4") } }
            styledDiv { css { classes.add("sk-cube sk-cube5") } }
            styledDiv { css { classes.add("sk-cube sk-cube6") } }
            styledDiv { css { classes.add("sk-cube sk-cube7") } }
            styledDiv { css { classes.add("sk-cube sk-cube8") } }
            styledDiv { css { classes.add("sk-cube sk-cube9") } }

            css {
                classes.add("sk-cube-grid")
                height = 60.px
                width = 60.px
            }
        }
    }
}