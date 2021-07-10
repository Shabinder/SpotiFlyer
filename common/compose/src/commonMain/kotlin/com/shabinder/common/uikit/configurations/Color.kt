/*
 * Copyright (c)  2021  Shabinder Singh
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.uikit.configurations

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

val colorPrimary = Color(0xFFFC5C7D)
val colorPrimaryDark = Color(0xFFCE1CFF)
val colorAccent = Color(0xFF9AB3FF)
val colorAccentVariant = Color(0xFF3457D5)
val colorRedError = Color(0xFFFF9494)
val colorSuccessGreen = Color(0xFF59C351)
val darkBackgroundColor = Color(0xFF000000)
val colorOffWhite = Color(0xFFE7E7E7)
val transparent = Color(0x00000000)
val black = Color(0xFF000000)
val lightGray = Color(0xFFCCCCCC)

val SpotiFlyerColors = darkColors(
    primary = colorPrimary,
    onPrimary = black,
    primaryVariant = colorPrimaryDark,
    secondary = colorAccent,
    onSecondary = black,
    error = colorRedError,
    onError = black,
    surface = darkBackgroundColor,
    background = darkBackgroundColor,
    onSurface = lightGray,
    onBackground = lightGray
)

/**
 * Return the fully opaque color that results from compositing [onSurface] atop [surface] with the
 * given [alpha]. Useful for situations where semi-transparent colors are undesirable.
 */
/*
@Composable
fun Colors.compositedOnSurface(alpha: Float): Color {
    return onSurface.copy(alpha = alpha).compositeOver(surface)
}*/
