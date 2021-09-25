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

package com.shabinder.common.uikit.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * Draws a vertical gradient scrim in the foreground.
 *
 * @param color The color of the gradient scrim.
 * @param startYPercentage The start y value, in percentage of the layout's height (0f to 1f)
 * @param endYPercentage The end y value, in percentage of the layout's height (0f to 1f)
 * @param decay The exponential decay to apply to the gradient. Defaults to `1.0f` which is
 * a linear gradient.
 * @param numStops The number of color stops to draw in the gradient. Higher numbers result in
 * the higher visual quality at the cost of draw performance. Defaults to `16`.
 */
fun Modifier.verticalGradientScrim(
    color: Color,
    /*@FloatRange(from = 0.0, to = 1.0)*/
    startYPercentage: Float = 0f,
    /*@FloatRange(from = 0.0, to = 1.0)*/
    endYPercentage: Float = 1f,
    decay: Float = 1.0f,
    numStops: Int = 16,
    fixedHeight: Float? = null
): Modifier = composed {
    val colors = remember(color, numStops) {
        if (decay != 1f) {
            // If we have a non-linear decay, we need to create the color gradient steps
            // manually
            val baseAlpha = color.alpha
            List(numStops) { i ->
                val x = i * 1f / (numStops - 1)
                val opacity = x.pow(decay)
                color.copy(alpha = baseAlpha * opacity)
            }
        } else {
            // If we have a linear decay, we just create a simple list of start + end colors
            listOf(color.copy(alpha = 0f), color)
        }
    }
    var height by remember { mutableStateOf(fixedHeight ?: 1f) }
    val scrimHeight by animateFloatAsState(
        // Whenever the target value changes, new animation
        // will start to the new target value
        targetValue = height,
        animationSpec = tween(durationMillis = 1500)
    )
    val brush = remember(color, numStops, startYPercentage, endYPercentage, scrimHeight) {
        Brush.verticalGradient(
            colors = colors,
            startY = scrimHeight * startYPercentage,
            endY = scrimHeight * endYPercentage
        )
    }

    drawBehind {
        height = fixedHeight ?: size.height
        drawRect(brush = brush)
    }
}
