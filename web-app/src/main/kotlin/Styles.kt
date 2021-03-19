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

import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderBottomColor
import kotlinx.css.borderBottomStyle
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.borderRightColor
import kotlinx.css.borderWidth
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.px
import styled.StyleSheet

val colorPrimary = Color("#FC5C7D")
val colorPrimaryDark = Color("#CE1CFF")
val colorAccent = Color("#9AB3FF")
val colorOffWhite = Color("#E7E7E7")

object Styles: StyleSheet("Searchbar", isStatic = true) {
    val makeRow by css {
        display = Display.flex
        alignItems = Align.center
        alignContent = Align.center
        justifyContent = JustifyContent.center
    }
    val darkMode by css {
        backgroundColor = Color.black
        color = Color.white
    }
    val circular by css {
        borderRadius = 30.px
        borderWidth = 5.px
        borderBottomStyle = BorderStyle.solid
    }
    val circularGradient by css {
        apply(circular)
        borderColor = Color.aqua
        borderBottomColor = colorPrimary
        borderRightColor = colorPrimary
    }
    val largePadding by css { padding(20.px) }
    val mediumPadding by css { padding(14.px) }
    val smallPadding by css { padding(4.px) }
    val largeMargin by css { margin(20.px) }
    val mediumMargin by css { margin(12.px) }
    val smallMargin by css { margin(4.px) }
}