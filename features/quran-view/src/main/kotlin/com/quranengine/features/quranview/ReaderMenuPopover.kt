package com.quranengine.features.quranview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.chromeBackground

private val CardWidth = 300.dp
private val CardMaxHeight = 320.dp

/**
 * Compact opaque menu card anchored next to [anchorInRoot], flipping above the
 * anchor when there is no room below. Falls back to bottom-centre without an anchor.
 */
@Composable
internal fun ReaderMenuPopover(
    anchorInRoot: Offset?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    var cardHeightPx by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        )

        val cardHeight = with(density) { cardHeightPx.toDp() }
        // Keep the card clear of the audio dock at the bottom of the reader.
        val bottomLimit = maxHeight - 72.dp
        val maxLeft = (maxWidth - CardWidth - 12.dp).coerceAtLeast(12.dp)

        val left: androidx.compose.ui.unit.Dp
        val top: androidx.compose.ui.unit.Dp
        if (anchorInRoot != null) {
            val anchorX = with(density) { anchorInRoot.x.toDp() }
            val anchorY = with(density) { anchorInRoot.y.toDp() }
            left = (anchorX - CardWidth / 2).coerceIn(12.dp, maxLeft)
            val below = anchorY + 16.dp
            top = if (below + cardHeight <= bottomLimit) {
                below
            } else {
                (anchorY - cardHeight - 16.dp).coerceAtLeast(12.dp)
            }
        } else {
            left = ((maxWidth - CardWidth) / 2).coerceAtLeast(12.dp)
            top = (bottomLimit - cardHeight).coerceAtLeast(12.dp)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = left, y = top)
                .width(CardWidth)
                .wrapContentHeight()
                .onSizeChanged { cardHeightPx = it.height }
                // Hide the first frame, before the height needed for placement is known.
                .alpha(if (cardHeightPx == 0) 0f else 1f),
            shape = RoundedCornerShape(16.dp),
            color = QuranTheme.colors.chromeBackground(),
            contentColor = QuranTheme.colors.text,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = CardMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                content = content,
            )
        }
    }
}

@Composable
internal fun ReaderMenuTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = QuranTheme.colors.text.copy(alpha = 0.55f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
internal fun ReaderMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = QuranTheme.colors.text,
    customIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = QuranTheme.colors.text,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranTheme.colors.text.copy(alpha = 0.5f),
                )
            }
        }
    }
}
