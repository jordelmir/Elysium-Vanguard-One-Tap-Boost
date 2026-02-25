package com.elysium.vanguard.boost

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.vanguard.boost.core.OptimizationEngine
import com.elysium.vanguard.boost.ui.components.*
import com.elysium.vanguard.boost.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { SPLASH, SETUP, DASHBOARD }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf(Screen.SPLASH) }
            when (screen) {
                Screen.SPLASH -> SplashScreen { screen = Screen.SETUP }
                Screen.SETUP -> SetupGuideScreen(onContinue = { screen = Screen.DASHBOARD })
                Screen.DASHBOARD -> DashboardScreen(onBack = { screen = Screen.SETUP })
            }
        }
    }
}

// ═══════════════════════════════════════════
// SPLASH SCREEN
// ═══════════════════════════════════════════
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val inf = rememberInfiniteTransition()
    val bgPulse by inf.animateFloat(0.0f, 0.15f, infiniteRepeatable(tween(3000), RepeatMode.Reverse))

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1.2f, tween(1000, easing = FastOutSlowInEasing))
            scale.animateTo(1f, tween(400))
        }
        launch {
            alpha.animateTo(1f, tween(800))
            delay(1800)
            alpha.animateTo(0f, tween(400))
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(CyanNeon.copy(alpha = bgPulse), Obsidian))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(130.dp).scale(scale.value).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(ElectricGreen.copy(alpha = 0.25f), Color.Transparent)))
                    .border(2.dp, CyanNeon.copy(alpha = alpha.value * 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("⚡", fontSize = 60.sp, modifier = Modifier.scale(scale.value)) }
            Spacer(Modifier.height(28.dp))
            Text(
                "ELYSIUM VANGUARD", color = Color.White.copy(alpha = alpha.value),
                fontSize = 18.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp, textAlign = TextAlign.Center
            )
            Text(
                "BOOST", color = CyanNeon.copy(alpha = alpha.value),
                fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 10.sp, textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════
// SETUP GUIDE SCREEN
// ═══════════════════════════════════════════
@Composable
fun SetupGuideScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    // Real Shizuku status — polled every 2s
    var shizukuConnected by remember { mutableStateOf(false) }
    var shizukuInstalled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            shizukuInstalled = try {
                context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0); true
            } catch (e: Exception) { false }
            shizukuConnected = try {
                rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) { false }
            delay(2000)
        }
    }

    val inf = rememberInfiniteTransition()
    val headerGlow by inf.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(3000), RepeatMode.Reverse))

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Obsidian, GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            // Header centered
            Text("GUÍA DE", color = SteelGray, fontSize = 11.sp, letterSpacing = 4.sp, textAlign = TextAlign.Center)
            Text(
                "CONFIGURACIÓN", color = CyanNeon.copy(alpha = headerGlow),
                fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Live Shizuku Status
            ShizukuStatusBadge(isConnected = shizukuConnected)

            // STEP 1
            StepCard(1, "INSTALAR SHIZUKU",
                "Shizuku permite ejecutar comandos de sistema sin root. Es necesario para el protocolo.",
                isCompleted = shizukuInstalled
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeonButton("▶ PLAY STORE", {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")))
                    }, Modifier.weight(1f), ElectricGreen)
                    NeonButton("◆ GITHUB", {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RikkaApps/Shizuku")))
                    }, Modifier.weight(1f), PlasmaViolet)
                }
            }

            // STEP 2
            StepCard(2, "OPCIONES DE DESARROLLADOR",
                "1. Abre Ajustes → Acerca del teléfono\n2. Toca \"Número de compilación\" 7 veces\n3. Verás: \"Eres desarrollador\""
            ) {
                NeonButton("⚙ ABRIR AJUSTES", {
                    context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
                }, Modifier.fillMaxWidth(), CyanNeon)
            }

            // STEP 3
            StepCard(3, "DEPURACIÓN INALÁMBRICA",
                "1. Ve a Opciones de Desarrollador\n2. Activa \"Depuración inalámbrica\"\n3. Acepta el diálogo"
            ) {
                NeonButton("⚙ OPCIONES DE DESARROLLADOR", {
                    try {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }, Modifier.fillMaxWidth(), NeonBlue)
            }

            // STEP 4
            StepCard(4, "EMPAREJAR E INICIAR SHIZUKU",
                "1. Abre Shizuku\n2. Toca \"Emparejar\" → ingresa el código\n3. Toca \"Iniciar\" en Shizuku",
                isCompleted = shizukuConnected
            ) {
                NeonButton("▶ ABRIR SHIZUKU", {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                        intent?.let { context.startActivity(it) }
                    } catch (e: Exception) {}
                }, Modifier.fillMaxWidth(), ElectricGreen)
            }

            Spacer(Modifier.height(8.dp))

            // Continue
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("CONTINUAR AL BOOST →", color = Obsidian, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ═══════════════════════════════════════════
// DASHBOARD SCREEN
// ═══════════════════════════════════════════
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    var isOptimizing by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("SISTEMA LISTO") }
    var isCompleted by remember { mutableStateOf(false) }
    var progressValue by remember { mutableFloatStateOf(0f) }

    // Real Shizuku polling
    var shizukuConnected by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            shizukuConnected = try {
                rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) { false }
            delay(2000)
        }
    }

    val inf = rememberInfiniteTransition()
    val titleGlow by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2500), RepeatMode.Reverse))
    val bgPulse by inf.animateFloat(0f, 0.06f, infiniteRepeatable(tween(5000), RepeatMode.Reverse))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                listOf(CyanNeon.copy(alpha = bgPulse), Obsidian, GradientEnd),
                radius = 1200f
            ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(44.dp))

            // Brand — centered
            Text("ELYSIUM VANGUARD", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                fontWeight = FontWeight.Light, letterSpacing = 6.sp, textAlign = TextAlign.Center)
            Text("BOOST", color = CyanNeon.copy(alpha = titleGlow), fontSize = 38.sp,
                fontWeight = FontWeight.Black, letterSpacing = 6.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            // Live Shizuku Status
            ShizukuStatusBadge(isConnected = shizukuConnected)

            Spacer(Modifier.height(20.dp))

            // Hexagon Boost
            HexagonButton(isOptimizing = isOptimizing) {
                if (!isOptimizing) {
                    if (shizukuConnected) {
                        isOptimizing = true
                        isCompleted = false
                        progressValue = 0f
                        OptimizationEngine.executeHyperAcceleration(
                            onProgress = { log, progress ->
                                logText = log
                                progressValue = progress
                            },
                            onComplete = {
                                isOptimizing = false
                                isCompleted = true
                                progressValue = 1f
                            }
                        )
                    } else {
                        try { rikka.shizuku.Shizuku.requestPermission(1001) }
                        catch (e: Exception) { logText = "⚠ CONECTA SHIZUKU PRIMERO" }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Status Grid — responsive 4 columns
            if (isCompleted) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip("CPU", "100%", CyanNeon, Modifier.weight(1f))
                    StatusChip("GPU", "MAX", ElectricGreen, Modifier.weight(1f))
                    StatusChip("Hz", "MAX", PlasmaViolet, Modifier.weight(1f))
                    StatusChip("TÁCTIL", "MAX", ToxicLime, Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip("CACHÉ", "x64", NeonBlue, Modifier.weight(1f))
                    StatusChip("AOT", "✓", ElectricGreen, Modifier.weight(1f))
                    StatusChip("TRIM", "✓", CyanNeon, Modifier.weight(1f))
                    StatusChip("GHOST", "ON", LaserRed, Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
            }

            // Terminal
            GlassCard(borderColor = if (isOptimizing) ElectricGreen else CyanNeon) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("TERMINAL", color = CyanNeon, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    if (isOptimizing) {
                        Text("${(progressValue * 100).toInt()}%", color = ElectricGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("▸ $logText", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progressValue }, modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (isCompleted) ElectricGreen else CyanNeon, trackColor = GlassWhite
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(if (isCompleted) "COMPLETO" else "PROTOCOLO: ELITE", color = SteelGray, fontSize = 9.sp)
                    Text(if (isCompleted) "STEALTH: ON" else if (isOptimizing) "EN PROGRESO..." else "ESPERANDO...", color = SteelGray, fontSize = 9.sp)
                }
            }

            // Protocols
            if (isCompleted) {
                Spacer(Modifier.height(10.dp))
                GlassCard(borderColor = ElectricGreen) {
                    Text("PROTOCOLOS ACTIVOS", color = ElectricGreen, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "⚡ Animaciones: 0.15x",
                        "📺 Refresh Rate: Máximo dispositivo",
                        "🔥 Performance: CPU/GPU fijo al 100%",
                        "💾 Caché: 64 procesos en RAM",
                        "🎮 GPU: SkiaGL Rendering",
                        "⏳ AOT: Compilación ahead-of-time",
                        "💿 FSTRIM: Storage optimizado",
                        "🔒 Stealth: Banking bypass activo"
                    ).forEach {
                        Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Back button
            NeonButton("← VOLVER A GUÍA", onBack, Modifier.fillMaxWidth(), SteelGray)

            Spacer(Modifier.height(40.dp))
        }
    }
}
