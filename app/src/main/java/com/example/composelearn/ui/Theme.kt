package com.example.composelearn.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ComposeLearnTheme(content: @Composable() () -> Unit) {
    MaterialTheme(
            colors = SpotiFlyerColors,
            typography = SpotiFlyerTypography,
            shapes = SpotiFlyerShapes,
            content = content
    )
}