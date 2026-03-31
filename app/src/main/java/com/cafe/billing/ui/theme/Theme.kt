package com.cafe.billing.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// APP THEME
// Material 3 color scheme tailored for a warm café aesthetic.
// Supports both light and dark modes.
// ============================================================

// ── Brand Colors ──────────────────────────────────────────
val CoffeeBrown       = Color(0xFF6D4C41)   // Primary – warm espresso brown
val CoffeeBrownDark   = Color(0xFF4E342E)   // Primary variant
val CoffeeBrownLight  = Color(0xFF9C786C)   // Primary container
val CreamWhite        = Color(0xFFFFF8F0)   // Background – warm off-white
val CardCream         = Color(0xFFFFF1E0)   // Surface – slightly deeper cream
val AccentAmber       = Color(0xFFFFB300)   // Secondary – golden amber
val AccentAmberDark   = Color(0xFFFF8F00)
val ErrorRed          = Color(0xFFD32F2F)

// ── Light Color Scheme ────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = CoffeeBrown,
    onPrimary          = Color.White,
    primaryContainer   = CoffeeBrownLight,
    onPrimaryContainer = Color(0xFF1A0A06),

    secondary          = AccentAmber,
    onSecondary        = Color(0xFF1A0A00),
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF1A0A00),

    background         = CreamWhite,
    onBackground       = Color(0xFF1C1410),

    surface            = CardCream,
    onSurface          = Color(0xFF1C1410),

    surfaceVariant     = Color(0xFFEDE0D4),
    onSurfaceVariant   = Color(0xFF4D3B33),

    error              = ErrorRed,
    onError            = Color.White,
)

// ── Dark Color Scheme ─────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Color(0xFFD4A896),
    onPrimary          = Color(0xFF3B1F17),
    primaryContainer   = CoffeeBrownDark,
    onPrimaryContainer = Color(0xFFFFDBCF),

    secondary          = Color(0xFFFFCC80),
    onSecondary        = Color(0xFF3B2000),
    secondaryContainer = Color(0xFF7A4F00),

    background         = Color(0xFF1C1410),
    onBackground       = Color(0xFFF0E4DC),

    surface            = Color(0xFF2A1F1A),
    onSurface          = Color(0xFFF0E4DC),

    surfaceVariant     = Color(0xFF4D3B33),
    onSurfaceVariant   = Color(0xFFD1B9AE),

    error              = Color(0xFFFF8A80),
    onError            = Color(0xFF601410),
)

/**
 * Main theme composable. Wrap your entire app in this.
 *
 * @param darkTheme  Pass true to force dark mode; defaults to system setting
 * @param content    Your composable content
 */
@Composable
fun CafeBillingTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),   // Uses Material 3 defaults
        content     = content
    )
}
