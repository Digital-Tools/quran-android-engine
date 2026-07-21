package com.quranengine.features.wordpointer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.ui.theme.QuranColors

private val TooltipVerticalOffset = 8.dp
private val TooltipPadding = 12.dp
private val TooltipCornerRadius = 8.dp
private val TooltipFontSize = 14.sp
private val HighlightColor = QuranColors.wordHighlight

@Composable
fun WordPointerOverlay(
    viewModel: WordPointerViewModel,
    frames: WordFrameCollection,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(frames) {
                detectDragGestures(
                    onDragStart = { offset ->
                        viewModel.onPanBegan()
                        viewModel.onPanMoved(offset.x, offset.y, frames)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val position = change.position
                        viewModel.onPanMoved(position.x, position.y, frames)
                    },
                    onDragEnd = {
                        viewModel.onPanEnded()
                    },
                    onDragCancel = {
                        viewModel.onPanEnded()
                    },
                )
            },
    ) {
        val currentState = state

        if (currentState is WordPointerState.Highlighting) {
            HighlightRect(currentState)
            TooltipPopup(currentState)
        }
    }
}

@Composable
private fun HighlightRect(state: WordPointerState.Highlighting) {
    val frame = state.frame
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = HighlightColor,
            topLeft = Offset(frame.minX.toFloat(), frame.minY.toFloat()),
            size = Size(
                width = (frame.maxX - frame.minX).toFloat(),
                height = (frame.maxY - frame.minY).toFloat(),
            ),
        )
    }
}

@Composable
private fun TooltipPopup(state: WordPointerState.Highlighting) {
    val text = state.text ?: return
    val frame = state.frame
    val density = LocalDensity.current

    val tooltipOffsetY = with(density) {
        frame.minY.toFloat() - TooltipVerticalOffset.toPx()
    }
    val tooltipOffsetX = ((frame.minX + frame.maxX) / 2f)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = tooltipOffsetX.toInt(),
                            y = tooltipOffsetY.toInt(),
                        )
                    }
                    .offset(x = (-TooltipPadding)),
                shape = RoundedCornerShape(TooltipCornerRadius),
                color = QuranColors.mizanGoldDark,
                shadowElevation = 4.dp,
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(
                        horizontal = TooltipPadding,
                        vertical = TooltipPadding / 2,
                    ),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = TooltipFontSize,
                )
            }
        }
    }
}
