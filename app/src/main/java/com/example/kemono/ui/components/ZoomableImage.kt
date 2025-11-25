package com.example.kemono.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ZoomableImage(model: Any?, contentDescription: String?, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
            modifier =
                    modifier.fillMaxSize().pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                val isZoomed = scale > 1f
                                val isMultiTouch = event.changes.size > 1

                                if (isZoomed || isMultiTouch) {
                                    scale = (scale * zoom).coerceIn(1f, 3f)

                                    if (scale > 1f) {
                                        val maxOffsetX = (size.width * (scale - 1)) / 2
                                        val maxOffsetY = (size.height * (scale - 1)) / 2
                                        offsetX =
                                                (offsetX + pan.x * scale).coerceIn(
                                                        -maxOffsetX,
                                                        maxOffsetX
                                                )
                                        offsetY =
                                                (offsetY + pan.y * scale).coerceIn(
                                                        -maxOffsetY,
                                                        maxOffsetY
                                                )
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
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
        AsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier =
                        Modifier.fillMaxSize()
                                .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                )
        )
    }
}
