package com.example.kemono.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.shimmerEffect(): Modifier = composed {
    var size = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * 1000f,
        targetValue = 2 * 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    background(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + 500f, 500f)
        )
    )
    .onGloballyPositioned {
        size.value = it.size
    }
}
