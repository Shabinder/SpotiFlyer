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

import kotlinx.css.*
import styled.StyleSheet

object NavBarStyles : StyleSheet("WelcomeStyles", isStatic = true) {
    val nav by css{
        padding(horizontal = 16.px)
        marginTop = 10.px
        backgroundColor = Color.transparent
        height = 56.px
        display = Display.flex
        flexDirection = FlexDirection.row
        alignItems = Align.center
        alignSelf = Align.stretch
    }
} 
