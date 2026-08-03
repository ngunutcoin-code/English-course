package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TealAccent

@Composable
fun AudioWaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val centerY = size.height / 2f
        val barWidth = 6.dp.toPx()
        val spacing = 4.dp.toPx()
        val maxBars = (size.width / (barWidth + spacing)).toInt()

        val displayAmplitudes = if (amplitudes.isEmpty() || !isRecording) {
            List(maxBars) { 0.15f }
        } else {
            val padded = amplitudes.takeLast(maxBars).toMutableList()
            while (padded.size < maxBars) {
                padded.add(0, 0.1f)
            }
            padded
        }

        val startX = (size.width - (displayAmplitudes.size * (barWidth + spacing))) / 2f

        displayAmplitudes.forEachIndexed { index, amp ->
            val x = startX + index * (barWidth + spacing)
            val barHeight = (amp * (size.height * 0.8f)).coerceAtLeast(8.dp.toPx())

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(PurpleLight, PurplePrimary, TealAccent)
                ),
                start = Offset(x, centerY - (barHeight / 2f)),
                end = Offset(x, centerY + (barHeight / 2f)),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
