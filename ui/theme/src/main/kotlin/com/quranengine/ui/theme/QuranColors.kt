package com.quranengine.ui.theme

import androidx.compose.ui.graphics.Color

object QuranColors {
    // Mizan brand green (`AppColors.brand` / `brandBright`)
    val appIdentityLight = Color(0xFF1A6B4A)
    val appIdentityDark = Color(0xFF2E9E72)

    // Mizan gold for mushaf / home top-bar buttons
    val mizanGoldLight = Color(0xFFD4AF37)
    val mizanGoldDark = Color(0xFFF1C40F)

    // Page Marker
    val pageMarkerLight = Color(0xFF004D40)
    val pageMarkerDark = Color(0xFF039F85)

    // Highlight colors (brand green @ 30% alpha)
    val wordHighlight = Color(0x4D1A6B4A)
    val readingHighlight = Color(0x4D1A6B4A)
    val shareHighlight = Color(0x4D2196F3) // blue at 0.3 alpha
    val searchHighlight = Color(0x4D9E9E9E) // gray at 0.3 alpha
}

data class ThemeColors(
    val text: Color,
    val arabicText: Color,
    val background: Color,
    val secondaryText: Color,
    val secondaryBackground: Color,
    val pageSeparatorBackground: Color,
    val pageSeparatorLine: Color,
)

// For secondary colors, use the iOS blending logic:
// Secondary text: blend with reference at factor 0.31, then alpha 0.6
// Secondary background: blend with reference at factor 0.31, then alpha 0.5
// Page separator line: blend label with white/black at factor ~0.212
// Page separator bg: blend label with white/black at factor ~0.118
// For dark labels (luminance < 0.5): blend with white; for light labels: blend with black

private fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}

private fun Color.blendWith(other: Color, factor: Float): Color {
    return Color(
        red = red * (1 - factor) + other.red * factor,
        green = green * (1 - factor) + other.green * factor,
        blue = blue * (1 - factor) + other.blue * factor,
        alpha = 1f,
    )
}

private fun Color.secondaryLabelVariant(): Color {
    val blendFactor = 0.31f
    val lightRef = Color(193f / 255f, 193f / 255f, 216f / 255f)
    val darkRef = Color(190f / 255f, 190f / 255f, 222f / 255f)
    val reference = if (luminance() < 0.5f) lightRef else darkRef
    return blendWith(reference, blendFactor).copy(alpha = 0.6f)
}

private fun Color.secondaryBackgroundVariant(): Color {
    val blendFactor = 0.31f
    val darkRef = Color(171f / 255f, 171f / 255f, 187f / 255f)
    val lightRef = Color(142f / 255f, 142f / 255f, 149f / 255f)
    val reference = if (luminance() > 0.5f) darkRef else lightRef
    return blendWith(reference, blendFactor).copy(alpha = 0.5f)
}

private fun Color.pageSeparatorLineVariant(): Color {
    val blendFactor = 1f - (201f / 255f) // ~0.212
    return if (luminance() < 0.5f) {
        blendWith(Color.White, blendFactor)
    } else {
        blendWith(Color.Black, blendFactor)
    }
}

private fun Color.pageSeparatorBackgroundVariant(): Color {
    val blendFactor = 1f - (225f / 255f) // ~0.118
    return if (luminance() < 0.5f) {
        blendWith(Color.White, blendFactor)
    } else {
        blendWith(Color.Black, blendFactor)
    }
}

/** Lift [background] toward white — used for chrome / audio dock fills (matches iOS). */
fun Color.liftTowardWhite(factor: Float): Color {
    return Color(
        red = red + (1f - red) * factor,
        green = green + (1f - green) * factor,
        blue = blue + (1f - blue) * factor,
        alpha = 1f,
    )
}

/** Mushaf top-bar fill: a bit lighter than the page. */
fun ThemeColors.chromeBackground(): Color = background.liftTowardWhite(0.12f)

/**
 * Bottom audio dock fill.
 * Light: near-white; dark: lighter grey than the page (lift ~0.18).
 */
fun ThemeColors.audioBannerBackground(isDark: Boolean): Color {
    val lift = if (isDark) 0.18f else 0.88f
    return background.liftTowardWhite(lift)
}

fun ThemeStyle.colors(isDark: Boolean): ThemeColors {
    val (text, bg) = when (this) {
        ThemeStyle.CALM -> if (isDark) {
            Color(0.969f, 0.925f, 0.867f) to Color(0.255f, 0.231f, 0.192f)
        } else {
            Color(0.196f, 0.157f, 0.118f) to Color(0.933f, 0.886f, 0.800f)
        }
        ThemeStyle.FOCUS -> if (isDark) {
            Color(0.996f, 0.976f, 0.925f) to Color(0.094f, 0.086f, 0.051f)
        } else {
            Color(0.078f, 0.071f, 0.008f) to Color(0.996f, 0.988f, 0.961f)
        }
        ThemeStyle.ORIGINAL -> if (isDark) {
            Color.White to Color.Black
        } else {
            Color.Black to Color.White
        }
        ThemeStyle.PAPER -> if (isDark) {
            Color(0.949f, 0.949f, 0.941f) to Color(0.110f, 0.110f, 0.118f)
        } else {
            Color(0.114f, 0.102f, 0.102f) to Color(0.933f, 0.929f, 0.929f)
        }
        ThemeStyle.QUIET -> if (isDark) {
            Color(0.553f, 0.553f, 0.573f) to Color.Black
        } else {
            Color(0.922f, 0.922f, 0.957f) to Color(0.290f, 0.290f, 0.302f)
        }
    }
    return ThemeColors(
        text = text,
        arabicText = if (isDark) {
            Color(0.949f, 0.949f, 0.941f)
        } else {
            text
        },
        background = bg,
        secondaryText = text.secondaryLabelVariant(),
        secondaryBackground = bg.secondaryBackgroundVariant(),
        pageSeparatorBackground = bg.pageSeparatorBackgroundVariant(),
        pageSeparatorLine = bg.pageSeparatorLineVariant(),
    )
}
