package com.example.core.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.bottomGlow(
    elevation: Dp,
    shape: Shape = RoundedCornerShape(24.dp)
) = this.then(
    Modifier.graphicsLayer {
        shadowElevation = elevation.toPx()
        this.shape = shape
        clip = false
    }
)