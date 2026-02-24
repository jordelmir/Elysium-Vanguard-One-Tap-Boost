package com.elysium.vanguard.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.vanguard.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HexagonButton(
    isOptimizing: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(280.dp)
            .clickable(onClick = onClick)
    ) {
        // Volumetric Outer Glow
        Canvas(modifier = Modifier.size(280.dp * glowScale).blur(40.dp)) {
            drawHexagon(brush = Brush.sweepGradient(listOf(CyanNeon, MagentaNeon, CyanNeon)), style = Fill)
        }

        // Animated Border
        Canvas(modifier = Modifier.size(220.dp).graphicsLayer(rotationZ = rotation)) {
            drawHexagon(
                brush = Brush.linearGradient(listOf(CyanNeon, MagentaNeon)),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Inner Hexagon (The Button)
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(HexagonShape)
                .background(Brush.verticalGradient(listOf(DeepCharcoal, Obsidian)))
                .border(2.dp, GlassWhite, HexagonShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isOptimizing) "RUNNING" else "V",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isOptimizing) "ENGINE ACTIVE" else "HYPER-ACCELERATE",
                    color = CyanNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

val HexagonShape = GenericShape { size, _ ->
    val radius = size.width / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    for (i in 0..5) {
        val angle = i * 60f * (Math.PI / 180f).toFloat()
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(
    brush: Brush,
    style: DrawStyle
) {
    val radius = size.width / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val path = Path().apply {
        for (i in 0..5) {
            val angle = i * 60f * (Math.PI / 180f).toFloat()
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, brush = brush, style = style)
}
