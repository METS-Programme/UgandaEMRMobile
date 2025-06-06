package com.lyecdevelopers.core.model

import androidx.compose.ui.graphics.Color


data class BrandColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val accent: Color,
    val accentVariant: Color,
    val surface: Color,
    val background: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val neutral: Color,
    val primaryDark: Color,
    val secondaryDark: Color,
    val accentDark: Color,
)


val DefaultBrandColors = BrandColors(
    primary = Color(0xFF009384),
    primaryVariant = Color(0xFF4DB6AC),
    secondary = Color(0xFF5C6BC0),
    secondaryVariant = Color(0xFF9FA8DA),
    accent = Color(0xFFFFC107),
    accentVariant = Color(0xFFFFE082),
    surface = Color(0xFFF5F5F5),
    background = Color(0xFFFFFFFF),
    error = Color(0xFFB00020),
    success = Color(0xFF388E3C),
    warning = Color(0xFFFFA000),
    info = Color(0xFF1976D2),
    neutral = Color(0xFF9E9E9E),
    primaryDark = Color(0xFF00675B),
    secondaryDark = Color(0xFF3949AB),
    accentDark = Color(0xFFFFA000)
)
