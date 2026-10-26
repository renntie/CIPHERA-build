package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Slate950

@Composable
fun CipheraLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        val gradient = Brush.linearGradient(
            colors = listOf(Emerald400, Cyan400),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // Outer Hexagonal Shield
        val outerPath = Path().apply {
            moveTo(cx, h * 0.06f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.92f, h * 0.72f)
            lineTo(cx, h * 0.94f)
            lineTo(w * 0.08f, h * 0.72f)
            lineTo(w * 0.08f, h * 0.28f)
            close()
        }

        drawPath(
            path = outerPath,
            brush = gradient,
            style = Stroke(width = w * 0.06f)
        )

        // Inner Hexagonal Dashed Circuit
        val innerPath = Path().apply {
            moveTo(cx, h * 0.18f)
            lineTo(w * 0.80f, h * 0.35f)
            lineTo(w * 0.80f, h * 0.65f)
            lineTo(cx, h * 0.82f)
            lineTo(w * 0.20f, h * 0.65f)
            lineTo(w * 0.20f, h * 0.35f)
            close()
        }

        drawPath(
            path = innerPath,
            color = Cyan400.copy(alpha = 0.5f),
            style = Stroke(
                width = w * 0.025f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 0.06f, w * 0.04f))
            )
        )

        // Center Keyhole Circle
        val circleRadius = w * 0.10f
        drawCircle(
            color = Slate950,
            radius = circleRadius,
            center = Offset(cx, cy - h * 0.06f)
        )
        drawCircle(
            brush = gradient,
            radius = circleRadius,
            center = Offset(cx, cy - h * 0.06f),
            style = Stroke(width = w * 0.045f)
        )

        // Keyhole Stem
        val stemPath = Path().apply {
            moveTo(cx - w * 0.045f, cy - h * 0.01f)
            lineTo(cx - w * 0.065f, cy + h * 0.18f)
            lineTo(cx + w * 0.065f, cy + h * 0.18f)
            lineTo(cx + w * 0.045f, cy - h * 0.01f)
            close()
        }
        drawPath(
            path = stemPath,
            brush = gradient
        )

        // Circuit Nodes
        val nodeRadius = w * 0.04f
        drawCircle(color = Emerald400, radius = nodeRadius, center = Offset(cx, h * 0.06f))
        drawCircle(color = Cyan400, radius = nodeRadius, center = Offset(w * 0.92f, h * 0.28f))
        drawCircle(color = Emerald400, radius = nodeRadius, center = Offset(w * 0.08f, h * 0.28f))
        drawCircle(color = Cyan400, radius = nodeRadius, center = Offset(cx, h * 0.94f))
    }
}
