package com.example.kemono.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import coil.compose.SubcomposeAsyncImage
import com.example.kemono.ui.components.shimmerEffect
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(model: Any?, contentDescription: String?, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            if (scale.value > 1f) {
                                scale.animateTo(1f)
                                offsetX.animateTo(0f)
                                offsetY.animateTo(0f)
                            } else {
                                scale.animateTo(3f)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        val isZoomed = scale.value > 1f
                        val isMultiTouch = event.changes.size > 1

                        if (isZoomed || isMultiTouch) {
                            scope.launch {
                                val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                                scale.snapTo(newScale)

                                if (newScale > 1f) {
                                    val maxOffsetX = (size.width * (newScale - 1)) / 2
                                    val maxOffsetY = (size.height * (newScale - 1)) / 2
                                    
                                    val newOffsetX = (offsetX.value + pan.x * newScale).coerceIn(-maxOffsetX, maxOffsetX)
                                    val newOffsetY = (offsetY.value + pan.y * newScale).coerceIn(-maxOffsetY, maxOffsetY)
                                    
                                    offsetX.snapTo(newOffsetX)
                                    offsetY.snapTo(newOffsetY)
                                } else {
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                            }

                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    it.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
            loading = {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }
        )
    }
}
