package com.elysium.vanguard.core

import android.os.Build
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.InputStream
import java.util.Scanner

object OptimizationEngine {

    const val TAG = "ElysiumEngine"

    // Core Protocol Commands
    private val BASE_COMMANDS = listOf(
        "settings put global window_animation_scale 0.15",
        "settings put global transition_animation_scale 0.15",
        "settings put global animator_duration_scale 0.15",
        "settings put secure min_refresh_rate 120.0",
        "settings put secure peak_refresh_rate 120.0",
        "settings put global high_refresh_rate_blacklist ''",
        "cmd power set-fixed-performance-mode-enabled true",
        "settings put global low_power 0",
        "settings put global adaptive_battery_management 0",
        "settings put global app_standby_enabled 0",
        "settings put system pointer_speed 7",
        "logcat -G 16M"
    )

    // Selective Debloat Lists
    private val HONOR_DEBLOAT = listOf(
        "com.hihonor.browserhomepage",
        "com.hihonor.android.totemweather",
        "com.hihonor.searchservice",
        "com.hihonor.awareness",
        "com.hihonor.msdp",
        "com.hihonor.visionengine",
        "com.hihonor.magicvoice"
    )

    fun executeHyperAcceleration(onProgress: (String) -> Unit, onComplete: () -> Unit) {
        Thread {
            try {
                onProgress("Iniciando Protocolo Elysium...")
                
                // 1. System Tweaks
                BASE_COMMANDS.forEach { cmd ->
                    runShellCommand(cmd)
                    onProgress("Aplicando: ${cmd.take(30)}...")
                }

                // 2. Hardware Specific Debloat
                if (Build.MANUFACTURER.contains("HONOR", ignoreCase = true)) {
                    onProgress("Optimizando MagicOS...")
                    HONOR_DEBLOAT.forEach { pkg ->
                        runShellCommand("pm disable-user --user 0 $pkg")
                        onProgress("Depurando: $pkg")
                    }
                }

                // 3. Stealth Mode (Final Step)
                runShellCommand("settings put global development_settings_enabled 0")
                
                onProgress("PROCESO COMPLETADO")
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error during acceleration", e)
                onProgress("ERROR: ${e.localizedMessage}")
            }
        }.start()
    }

    private fun runShellCommand(cmd: String) {
        // En algunas versiones de Shizuku 13.1.5, newProcess puede estar marcado como privado o restringido.
        // Utilizamos reflexión como puente de alta compatibilidad para garantizar la ejecución.
        try {
            val shizukuClass = rikka.shizuku.Shizuku::class.java
            val newProcessMethod = shizukuClass.getDeclaredMethod(
                "newProcess", 
                Array<String>::class.java, 
                Array<String>::class.java, 
                String::class.java
            )
            newProcessMethod.isAccessible = true
            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", cmd), null, null)
            
            // Esperar a que el proceso termine (ShizukuRemoteProcess suele tener waitFor)
            val waitForMethod = process.javaClass.getMethod("waitFor")
            waitForMethod.invoke(process)
        } catch (e: Exception) {
            Log.e(TAG, "Elysium Shell Error: ${e.message}")
            // Fallback silencioso para no interrumpir el flujo del UI si falla un comando menor
        }
    }
}
