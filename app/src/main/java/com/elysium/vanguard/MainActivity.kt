package com.elysium.vanguard.boost.boost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.vanguard.boost.boost.core.OptimizationEngine
import com.elysium.vanguard.boost.boost.ui.components.HexagonButton
import com.elysium.vanguard.boost.boost.ui.theme.*
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(this)
        
        setContent {
            ElysiumApp()
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        // Handle post-auth logic if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(this)
    }
}

@Composable
fun ElysiumApp() {
    var isOptimizing by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("SISTEMA LISTO") }
    var progress by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Obsidian, GradientEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier.padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ELYSIUM",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 8.sp
                )
                Text(
                    "VANGUARD",
                    color = CyanNeon,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Core Trigger
            HexagonButton(isOptimizing = isOptimizing) {
                if (!isOptimizing) {
                    if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        isOptimizing = true
                        OptimizationEngine.executeHyperAcceleration(
                            onProgress = { log -> logText = log },
                            onComplete = { isOptimizing = false }
                        )
                    } else {
                        Shizuku.requestPermission(1001)
                    }
                }
            }

            // Status Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = logText,
                    color = MagentaNeon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = if (isOptimizing) 0.5f else 0f, // Simplified for UI
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = CyanNeon,
                    trackColor = GlassWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LATENCIA: 0.15ms", color = Color.Gray, fontSize = 10.sp)
                    Text("STATUS: ELITE", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}
