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
            
            // Global Responsive container
            Surface(modifier = Modifier.fillMaxSize(), color = Obsidian) {
                when (screen) {
                    Screen.SPLASH -> SplashScreen { screen = Screen.SETUP }
                    Screen.SETUP -> SetupGuideScreen(onContinue = { screen = Screen.DASHBOARD })
                    Screen.DASHBOARD -> DashboardScreen(onBack = { screen = Screen.SETUP })
                }
            }
        }
    }
}

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp).scale(scale.value).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(ElectricGreen.copy(alpha = 0.25f), Color.Transparent)))
                    .border(2.dp, CyanNeon.copy(alpha = alpha.value * 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("⚡", fontSize = 60.sp) }
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

@Composable
fun SetupGuideScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var shizukuConnected by remember { mutableStateOf(false) }
    var shizukuInstalled by remember { mutableStateOf(false) }

    // Improved real-time polling with Shell Heartbeat
    LaunchedEffect(Unit) {
        while (true) {
            shizukuInstalled = try {
                context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0); true
            } catch (e: Exception) { false }
            shizukuConnected = OptimizationEngine.isShizukuFunctional()
            delay(2000)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Obsidian, GradientEnd)))) {
        val contentWidth = if (maxWidth > 600.dp) 560.dp else maxWidth
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(contentWidth)) {
                Text("GUÍA DE", color = SteelGray, fontSize = 11.sp, letterSpacing = 4.sp, textAlign = TextAlign.Center)
                Text(
                    "CONFIGURACIÓN", color = CyanNeon,
                    fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                ShizukuStatusBadge(isConnected = shizukuConnected)
                Spacer(Modifier.height(16.dp))

                StepCard(1, "INSTALAR SHIZUKU", "Optimización de sistema via Shell sin root.", shizukuInstalled) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeonButton("PLAY STORE", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))) }, Modifier.weight(1f), ElectricGreen)
                        NeonButton("GITHUB", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RikkaApps/Shizuku"))) }, Modifier.weight(1f), PlasmaViolet)
                    }
                }

                Spacer(Modifier.height(12.dp))
                StepCard(2, "OPCIONES DE DESARROLLADOR", "Activa el menú oculto de depuración.", false) {
                    NeonButton("PASO A PASO", { context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)) }, Modifier.fillMaxWidth(), CyanNeon)
                }

                Spacer(Modifier.height(12.dp))
                StepCard(3, "DEPURACIÓN INALÁMBRICA", "Permite la inyección de comandos.", false) {
                    NeonButton("IR A DESARROLLO", {
                        try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) } 
                        catch (e: Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
                    }, Modifier.fillMaxWidth(), NeonBlue)
                }

                Spacer(Modifier.height(12.dp))
                StepCard(4, "INICIAR SHIZUKU", "Sincroniza el ADB con la aplicación.", shizukuConnected) {
                    NeonButton("ABRIR SHIZUKU", {
                        try { context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")?.let { context.startActivity(it) } } 
                        catch (e: Exception) {}
                    }, Modifier.fillMaxWidth(), ElectricGreen)
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("PROCEDER AL PANEL →", color = Obsidian, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun DashboardScreen(onBack: () -> Unit) {
    var isOptimizing by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("PROTOCOLO LISTO") }
    var isBoosted by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf(0f) }
    var shizukuConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            shizukuConnected = OptimizationEngine.isShizukuFunctional()
            delay(2000)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(MidnightBlue, Obsidian), radius = 1000f))) {
        val contentWidth = if (maxWidth > 600.dp) 560.dp else maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(contentWidth)) {
                Text("ELYSIUM VANGUARD", color = SteelGray, fontSize = 12.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp, textAlign = TextAlign.Center)
                Text("BOOST", color = CyanNeon, fontSize = 38.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp, textAlign = TextAlign.Center)

                Spacer(Modifier.height(16.dp))
                ShizukuStatusBadge(isConnected = shizukuConnected)

                Spacer(Modifier.height(24.dp))
                HexagonButton(isOptimizing = isOptimizing) {
                    if (!isOptimizing && !isRestoring && shizukuConnected) {
                        isOptimizing = true
                        progressValue = 0f
                        OptimizationEngine.executeHyperAcceleration(
                            onProgress = { msg, p -> logText = msg; progressValue = p },
                            onComplete = { isOptimizing = false; isBoosted = true; progressValue = 1f }
                        )
                    } else if (!shizukuConnected) {
                        logText = "⚠ ERROR: SHIZUKU NO FUNCIONAL"
                    }
                }

                Spacer(Modifier.height(24.dp))
                if (isBoosted || isOptimizing) {
                    StatusGrid()
                    Spacer(Modifier.height(16.dp))
                }

                GlassCard(borderColor = if (isOptimizing) ElectricGreen else if (isRestoring) ToxicLime else CyanNeon) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("SISTEMA LOG", color = CyanNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        if (isOptimizing || isRestoring) {
                            Text("${(progressValue * 100).toInt()}%", color = ElectricGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("▸ $logText", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Start)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progressValue }, 
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = if (isBoosted) ElectricGreen else CyanNeon, 
                        trackColor = GlassWhite
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeonButton("RESTAURAR", {
                        if (!isOptimizing && !isRestoring && shizukuConnected) {
                            isRestoring = true
                            progressValue = 0f
                            OptimizationEngine.executeRestoration(
                                onProgress = { msg, p -> logText = msg; progressValue = p },
                                onComplete = { isRestoring = false; isBoosted = false; progressValue = 1f; logText = "RESTORE COMPLETO" }
                            )
                        }
                    }, Modifier.weight(1f), ToxicLime)
                    
                    NeonButton("GUÍA", onBack, Modifier.weight(1f), SteelGray)
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun StatusGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatusChip("CPU", "MAX", CyanNeon, Modifier.weight(1f))
            StatusChip("GPU", "BOOST", ElectricGreen, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatusChip("TÁCTIL", "TURBO", NeonBlue, Modifier.weight(1f))
            StatusChip("CACHÉ", "x64", PlasmaViolet, Modifier.weight(1f))
        }
    }
}
