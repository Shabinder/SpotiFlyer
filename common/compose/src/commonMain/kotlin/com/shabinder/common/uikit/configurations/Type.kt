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

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

expect fun montserratFont(): FontFamily
expect fun pristineFont(): FontFamily

val SpotiFlyerTypography = Typography(
    h1 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 96.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 117.sp,
        letterSpacing = (-1.5).sp
    ),
    h2 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 60.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 73.sp,
        letterSpacing = (-0.5).sp
    ),
    h3 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 48.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 59.sp
    ),
    h4 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 37.sp
    ),
    h5 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 29.sp
    ),
    h6 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp

    ),
    subtitle1 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    ),
    body2 = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        letterSpacing = 1.25.sp
    ),
    caption = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    overline = TextStyle(
        fontFamily = montserratFont(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    )
)

val appNameStyle = TextStyle(
    fontFamily = pristineFont(),
    fontSize = 40.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 42.sp,
    letterSpacing = (1.5).sp,
    color = Color(0xFFECECEC)
)
