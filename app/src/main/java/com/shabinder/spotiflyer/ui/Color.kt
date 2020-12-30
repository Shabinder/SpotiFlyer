package com.shabinder.spotiflyer.ui

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

val colorPrimary = Color(0xFFFC5C7D)
val colorPrimaryDark = Color(0xFFCE1CFF)
val colorAccent = Color(0xFF9AB3FF)
val colorRedError = Color(0xFFFF9494)
val colorSuccessGreen = Color(0xFF59C351)
val darkBackgroundColor = Color(0xFF000000)
val colorOffWhite = Color(0xFFE7E7E7)

val SpotiFlyerColors = darkColors(
    primary = colorPrimary,
    onPrimary = Color.Black,
    primaryVariant = colorPrimaryDark,
    secondary = colorAccent,
    onSecondary = Color.Black,
    error = colorRedError,
    onError = Color.Black,
    surface = darkBackgroundColor,
    background = darkBackgroundColor,
    onSurface = Color.LightGray,
    onBackground = Color.LightGray
)

/**
 * Return the fully opaque color that results from compositing [onSurface] atop [surface] with the
 * given [alpha]. Useful for situations where semi-transparent colors are undesirable.
 */
@Composable
fun Colors.compositedOnSurface(alpha: Float): Color {
    return onSurface.copy(alpha = alpha).compositeOver(surface)
}