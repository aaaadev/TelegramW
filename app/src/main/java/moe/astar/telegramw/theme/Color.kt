package moe.astar.telegramw.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val telegramBlue = Color(0xFF0088CC)
val telegramLightBlue = Color(0xFF3F8FBB)
val telegramDarkBlue = Color(0xFF084B6E)
val telegramGreen = Color(0xFF00AF9C)
val telegramRed = Color(0xFFD70A00)
val telegramBlack = Color(0xFF000000)

internal val wearColorPalette: Colors = Colors(
    primary = telegramBlue,
    primaryVariant = telegramDarkBlue,
    secondary = telegramGreen,
    secondaryVariant = telegramGreen,
    error = telegramRed,
    onPrimary = telegramBlack,
    onSecondary = telegramBlack,
    onError = telegramBlack,
)