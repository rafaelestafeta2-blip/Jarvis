package com.example.jarvis.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlowBlue
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.NeonPurple

@Composable
fun PulseOrbVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 2000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isListening) 1.25f else if (isSpeaking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 600 else 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = if (isListening) NeonCyan else if (isSpeaking) ElectricViolet else GlowBlue
    val secondaryColor = if (isListening) NeonPurple else GlowBlue

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2.8f

            // Outer subtle glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f * pulseScale), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f * pulseScale
                ),
                radius = baseRadius * 1.5f * pulseScale,
                center = center
            )

            // Rotating tech ring
            rotate(rotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, secondaryColor, primaryColor),
                        center = center
                    ),
                    radius = baseRadius * pulseScale,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Counter-rotating inner arc ring
            rotate(-rotation * 1.5f, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.6f),
                    radius = baseRadius * 0.7f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Inner energy core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primaryColor),
                    center = center,
                    radius = baseRadius * 0.45f
                ),
                radius = baseRadius * 0.45f * pulseScale,
                center = center
            )
        }
    }
}
