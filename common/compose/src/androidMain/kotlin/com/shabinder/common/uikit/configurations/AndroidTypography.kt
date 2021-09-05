package com.shabinder.common.uikit.configurations

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.shabinder.common.models.R

actual fun montserratFont() = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
)

actual fun pristineFont(): FontFamily = FontFamily(
    Font(R.font.pristine_script, FontWeight.Bold)
)
