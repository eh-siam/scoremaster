package com.example.scoremaster.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val PremiumNavyLightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = OnNavy,
    primaryContainer = GreenLightContainer,
    onPrimaryContainer = OnGreenContainer,

    secondary = FreshElectricGreen,
    onSecondary = PureWhite,
    secondaryContainer = GreenLightContainer,
    onSecondaryContainer = OnGreenContainer,

    tertiary = FreshElectricGreen,
    onTertiary = PureWhite,
    tertiaryContainer = GreenLightContainer,
    onTertiaryContainer = OnGreenContainer,

    background = AppBackground,
    onBackground = PrimaryText,

    surface = PureWhite,
    onSurface = PrimaryText,
    surfaceVariant = AppBackground,
    onSurfaceVariant = SecondaryText,
    surfaceContainer = PureWhite,
    surfaceContainerHigh = AppBackground,
    surfaceContainerLow = AppBackground,

    outline = SubtleCardStroke,
    outlineVariant = SubtleCardStroke,

    error = LiveCrimsonRed,
    onError = OnLiveRed,
    errorContainer = RedLightContainer,
    onErrorContainer = OnRedContainer
)

@Composable
fun ScoreMasterTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumNavyLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.statusBarColor = DeepNavy.toArgb()
            window?.navigationBarColor = AppBackground.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
