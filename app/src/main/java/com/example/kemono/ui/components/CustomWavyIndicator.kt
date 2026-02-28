package com.example.kemono.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A custom, mathematically drawn wavy progress indicator that mimics Material 3's LinearWavyProgressIndicator! (o^▽^o)
 * It uses a sine wave and an infinite phase shift animation to look like it's flowing!
 */
@Composable
fun CustomWavyIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null, // null means indeterminate
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.2f),
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 4.dp, // How tall the waves are
    waveLength: Dp = 40.dp // How wide one full wave is
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    
    // Animate the phase shift from 0 to 2*PI endlessly
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(modifier = modifier.height(amplitude * 2 + strokeWidth)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(amplitude * 2 + strokeWidth)) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            
            val ampPx = amplitude.toPx()
            val waveLengthPx = waveLength.toPx()
            val strokeWidthPx = strokeWidth.toPx()

            val path = Path()
            
            // We draw the wave segment by segment across the canvas width
            val points = (width / 2f).toInt() // Draw enough points for a smooth curve
            
            for (i in 0..points) {
                val x = (i.toFloat() / points) * width
                
                val angularFrequency = (2 * PI) / waveLengthPx
                val y = midY + ampPx * sin((angularFrequency * x) + phase).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Draw the background track (unfilled portion)
            drawPath(
                path = path,
                color = trackColor,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Draw the active progress track overlaid and clipped
            if (progress != null) {
                clipRect(right = width * progress.coerceIn(0f, 1f)) {
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            } else {
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}
