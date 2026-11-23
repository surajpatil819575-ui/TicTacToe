package com.example.tictactoe.games.tictactoe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// yahan AnimatedMark aur WinLine paste karo
// ---------- X / O animation ----------
@Composable
fun AnimatedMark(mark: String, xColor: Color, oColor: Color, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(mark) {
        progress.snapTo(0f)
        if (mark.isNotEmpty()) progress.animateTo(1f, tween(400))
    }

    Canvas(modifier) {
        val p = progress.value
        val pad = size.minDimension * 0.25f
        val strokeW = 10.dp.toPx()

        when (mark) {
            "X" -> {
                val p1 = (p * 2f).coerceIn(0f, 1f)
                val p2 = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
                val a = Offset(pad, pad)
                val b = Offset(size.width - pad, size.height - pad)
                drawLine(xColor, a, lerp(a, b, p1), strokeW, StrokeCap.Round)
                if (p2 > 0f) {
                    val c = Offset(size.width - pad, pad)
                    val d = Offset(pad, size.height - pad)
                    drawLine(xColor, c, lerp(c, d, p2), strokeW, StrokeCap.Round)
                }
            }
            "O" -> drawArc(
                color = oColor,
                startAngle = -90f,
                sweepAngle = 360f * p,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = Size(size.width - 2 * pad, size.height - 2 * pad),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }
}

// ---------- Win line ----------
@Composable
fun WinLine(line: List<Int>?, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(line) {
        progress.snapTo(0f)
        if (line != null) progress.animateTo(1f, tween(500, delayMillis = 400))
    }

    Canvas(modifier) {
        if (line != null) {
            val cw = size.width / 3f
            val ch = size.height / 3f
            fun center(i: Int) = Offset((i % 3 + 0.5f) * cw, (i / 3 + 0.5f) * ch)

            val start = center(line[0])
            val end = center(line[2])
            drawLine(
                color = Color(0xFFEF5350),
                start = start,
                end = lerp(start, end, progress.value),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}