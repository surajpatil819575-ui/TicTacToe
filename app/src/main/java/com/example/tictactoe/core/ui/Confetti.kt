package com.example.tictactoe.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

// yahan Particle, confettiColors, Confetti paste karo
// ---------- Confetti ----------
class Particle(
    val x: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val drift: Float,
    val phase: Float,
    val spin: Float
)

val confettiColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFDD835), Color(0xFF8E24AA), Color(0xFFFB8C00)
)

@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val particles = remember {
        List(90) {
            Particle(
                x = Random.nextFloat(),
                speed = 1f + Random.nextFloat() * 0.6f,
                size = 6f + Random.nextFloat() * 6f,
                color = confettiColors.random(),
                drift = 10f + Random.nextFloat() * 30f,
                phase = Random.nextFloat() * 6.28f,
                spin = 0.5f + Random.nextFloat()
            )
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(3500, easing = LinearEasing)) }

    Canvas(modifier) {
        for (p in particles) {
            val prog = t.value * p.speed
            val y = -20f + prog * (size.height + 40f)
            if (y > size.height) continue
            val x = p.x * size.width + sin(prog * 10f + p.phase) * p.drift.dp.toPx()
            val s = p.size.dp.toPx()
            rotate(degrees = prog * 720f * p.spin, pivot = Offset(x, y)) {
                drawRect(p.color, Offset(x - s / 2, y - s / 2), Size(s, s * 0.6f))
            }
        }
    }
}