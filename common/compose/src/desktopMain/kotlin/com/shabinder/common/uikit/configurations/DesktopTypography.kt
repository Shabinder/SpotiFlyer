package com.shabinder.common.uikit.configurations

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual fun montserratFont() = FontFamily(
    Font("font/montserrat_light.ttf", FontWeight.Light),
    Font("font/montserrat_regular.ttf", FontWeight.Normal),
    Font("font/montserrat_medium.ttf", FontWeight.Medium),
    Font("font/montserrat_semibold.ttf", FontWeight.SemiBold),
)

actual fun pristineFont() = FontFamily(
    Font("font/pristine_script.ttf", FontWeight.Bold)
)
