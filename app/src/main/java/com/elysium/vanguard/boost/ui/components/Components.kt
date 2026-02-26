package com.elysium.vanguard.boost.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.vanguard.boost.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyanNeon,
    content: @Composable ColumnScope.() -> Unit
) {
    val inf = rememberInfiniteTransition()
    val borderAlpha by inf.animateFloat(0.15f, 0.5f, infiniteRepeatable(tween(2500), RepeatMode.Reverse))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .border(1.2.dp, borderColor.copy(alpha = borderAlpha), RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun NeonButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, color: Color = CyanNeon
) {
    val inf = rememberInfiniteTransition()
    val glow by inf.animateFloat(0.08f, 0.25f, infiniteRepeatable(tween(1800), RepeatMode.Reverse))

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = glow)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatusChip(label: String, value: String, color: Color = CyanNeon, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition()
    val pulse by inf.animateFloat(0.2f, 0.7f, infiniteRepeatable(tween(2000), RepeatMode.Reverse))

    Column(
        modifier = modifier
            .background(SurfaceCard.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = pulse), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = SteelGray, fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
fun ShizukuStatusBadge(isConnected: Boolean) {
    val inf = rememberInfiniteTransition()
    val dotAlpha by inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(if (isConnected) 1200 else 600), RepeatMode.Reverse))
    val color = if (isConnected) ElectricGreen else LaserRed
    val text = if (isConnected) "SHIZUKU: ACTIVO" else "SHIZUKU: REQUERIDO"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color.copy(alpha = dotAlpha)))
        Spacer(Modifier.width(10.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun StepCard(
    stepNumber: Int, title: String, description: String,
    isCompleted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val inf = rememberInfiniteTransition()
    val borderAlpha by inf.animateFloat(0.1f, 0.4f, infiniteRepeatable(tween(2000 + stepNumber * 300), RepeatMode.Reverse))
    val accent = if (isCompleted) ElectricGreen else CyanNeon

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = borderAlpha), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(accent.copy(alpha = 0.12f))
                .border(1.5.dp, accent.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isCompleted) "✓" else "$stepNumber", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(description, color = SteelGray, fontSize = 11.sp, lineHeight = 17.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun HexagonButton(isOptimizing: Boolean, onClick: () -> Unit) {
    val inf = rememberInfiniteTransition()
    val glowScale by inf.animateFloat(1f, 1.15f, infiniteRepeatable(tween(2000), RepeatMode.Reverse))
    val rotation by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart))
    val pulseAlpha by inf.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse))
    val innerGlow by inf.animateFloat(0.05f, 0.2f, infiniteRepeatable(tween(3000), RepeatMode.Reverse))

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp).clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.size(260.dp * glowScale)) {
            drawHexagon(Brush.radialGradient(listOf(CyanNeon.copy(alpha = 0.3f), Color.Transparent)), Fill)
        }
        Canvas(modifier = Modifier.graphicsLayer { rotationZ = rotation }.size(200.dp)) {
            drawHexagon(Brush.sweepGradient(listOf(CyanNeon, ElectricGreen, PlasmaViolet, CyanNeon)), Stroke(3.dp.toPx()))
        }
        Box(
            modifier = Modifier.size(160.dp).clip(HexagonShape)
                .background(Brush.verticalGradient(listOf(DeepCharcoal, Obsidian)))
                .border(1.dp, CyanNeon.copy(alpha = innerGlow), HexagonShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isOptimizing) "⚡" else "V",
                    color = if (isOptimizing) ElectricGreen.copy(alpha = pulseAlpha) else Color.White,
                    fontSize = 42.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center
                )
                Text(
                    if (isOptimizing) "PROCESANDO" else "ACCELERATE",
                    color = CyanNeon.copy(alpha = if (isOptimizing) pulseAlpha else 0.9f),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, textAlign = TextAlign.Center
                )
            }
        }
    }
}

val HexagonShape = GenericShape { size, _ ->
    val r = size.width / 2f; val cx = r; val cy = size.height / 2f
    for (i in 0..5) {
        val a = i * 60f * (Math.PI / 180f).toFloat()
        val x = cx + r * cos(a); val y = cy + r * sin(a)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(brush: Brush, style: DrawStyle) {
    val r = size.width / 2f; val cx = r; val cy = size.height / 2f
    val path = Path().apply {
        for (i in 0..5) {
            val a = i * 60f * (Math.PI / 180f).toFloat()
            if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
            else lineTo(cx + r * cos(a), cy + r * sin(a))
        }
        close()
    }
    drawPath(path, brush, style = style)
}
