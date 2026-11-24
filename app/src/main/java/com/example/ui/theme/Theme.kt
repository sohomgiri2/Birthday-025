package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CosmicDarkColorScheme = darkColorScheme(
    primary = NebulaViolet,
    onPrimary = SilverMist,
    primaryContainer = DeepSpaceSurface,
    onPrimaryContainer = SoftLavender,
    secondary = SkyCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = MidnightBlue,
    onSecondaryContainer = SilverMist,
    tertiary = CandleAmber,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = SilverMist,
    surface = DeepSpaceSurface,
    onSurface = SilverMist,
    surfaceVariant = MidnightBlue,
    onSurfaceVariant = MutedSilver,
    outline = GlassBorderSilver,
    error = RoseQuartz,
    onError = ObsidianBlack
)

private val FrostMidnightColorScheme = darkColorScheme(
    primary = SoftLavender,
    onPrimary = ObsidianBlack,
    primaryContainer = FrostSurface,
    onPrimaryContainer = SilverMist,
    secondary = AuroraMint,
    onSecondary = ObsidianBlack,
    secondaryContainer = FrostBackground,
    onSecondaryContainer = SilverMist,
    tertiary = StarlightGold,
    onTertiary = ObsidianBlack,
    background = FrostBackground,
    onBackground = SilverMist,
    surface = FrostSurface,
    onSurface = SilverMist,
    surfaceVariant = MidnightBlue,
    onSurfaceVariant = SilverMist,
    outline = FrostBorder,
    error = RoseQuartz,
    onError = ObsidianBlack
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun LittleWorldTheme(
    frostVariation: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (frostVariation) FrostMidnightColorScheme else CosmicDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
