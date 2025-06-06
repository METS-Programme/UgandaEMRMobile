package com.lyecdevelopers.core.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lyecdevelopers.core.model.BrandColors
import com.lyecdevelopers.core.model.DefaultBrandColors


@Composable
fun UgandaEMRMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    brandColors: BrandColors = DefaultBrandColors,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = brandColors.primary,
            secondary = brandColors.secondary,
            tertiary = brandColors.accent,
            background = brandColors.background,
            surface = brandColors.surface,
            error = brandColors.error,
        )

        else -> lightColorScheme(
            primary = brandColors.primaryVariant,
            secondary = brandColors.secondaryVariant,
            tertiary = brandColors.accentVariant,
            background = brandColors.background,
            surface = brandColors.surface,
            error = brandColors.error,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

