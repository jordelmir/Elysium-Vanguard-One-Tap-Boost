package com.elysium.vanguard.boost.core

import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object OptimizationEngine {

    const val TAG = "ElysiumEngine"

    // ═══ BOOST SETTINGS ═══
    private val PHASE_ANIMATIONS_BOOST = listOf(
        "settings put global window_animation_scale 0.15",
        "settings put global transition_animation_scale 0.15",
        "settings put global animator_duration_scale 0.15"
    )
    private val PHASE_DISPLAY_BOOST = listOf(
        "settings put secure min_refresh_rate 120.0",
        "settings put secure peak_refresh_rate 120.0",
        "settings put system min_refresh_rate 120.0",
        "settings put system peak_refresh_rate 120.0",
        "settings put global high_refresh_rate_blacklist ''",
        "settings put system pointer_speed 7"
    )
    private val PHASE_PERFORMANCE_BOOST = listOf(
        "cmd power set-fixed-performance-mode-enabled true",
        "settings put global low_power 0",
        "settings put global adaptive_battery_management_enabled 0",
        "settings put global adaptive_battery_management 0",
        "settings put global app_standby_enabled 0"
    )
    private val PHASE_CACHE_BOOST = listOf(
        "settings put global activity_manager_constants max_cached_processes=64"
    )

    // ═══ RESTORE SETTINGS (Android Defaults) ═══
    private val PHASE_ANIMATIONS_RESTORE = listOf(
        "settings put global window_animation_scale 1.0",
        "settings put global transition_animation_scale 1.0",
        "settings put global animator_duration_scale 1.0"
    )
    private val PHASE_DISPLAY_RESTORE = listOf(
        "settings delete secure min_refresh_rate",
        "settings delete secure peak_refresh_rate",
        "settings delete system min_refresh_rate",
        "settings delete system peak_refresh_rate",
        "settings put system pointer_speed 0"
    )
    private val PHASE_PERFORMANCE_RESTORE = listOf(
        "cmd power set-fixed-performance-mode-enabled false",
        "settings put global low_power 0",
        "settings put global adaptive_battery_management_enabled 1",
        "settings put global app_standby_enabled 1"
    )
    private val PHASE_CACHE_RESTORE = listOf(
        "settings delete global activity_manager_constants"
    )

    private val PHASE_STEALTH_RESTORE = listOf(
        "settings put global development_settings_enabled 1"
    )

    /**
     * Professional Detection: Combines binder ping + active shell command execution.
     */
    fun isShizukuFunctional(): Boolean {
        return try {
            val binderAlive = try { rikka.shizuku.Shizuku.pingBinder() } catch (e: Exception) { false }
            if (!binderAlive) return false
            
            val output = runShellCommandWithOutput("echo 1").trim()
            output == "1"
        } catch (e: Exception) {
            false
        }
    }

    fun executeHyperAcceleration(onProgress: (String, Float) -> Unit, onComplete: () -> Unit) {
        Thread {
            try {
                val totalSteps = 8f
                var current = 0f

                onProgress("⚡ APLICANDO ANIMACIONES (0.15x)", current++ / totalSteps)
                PHASE_ANIMATIONS_BOOST.forEach { runShellCommand(it) }

                onProgress("📺 MAXIMIZANDO REFRESH RATE", current++ / totalSteps)
                PHASE_DISPLAY_BOOST.forEach { runShellCommand(it) }

                onProgress("🔥 MODO RENDIMIENTO FIJO", current++ / totalSteps)
                PHASE_PERFORMANCE_BOOST.forEach { runShellCommand(it) }

                onProgress("💾 EXPANSIÓN DE CACHÉ", current++ / totalSteps)
                PHASE_CACHE_BOOST.forEach { runShellCommand(it) }

                onProgress("🎮 OPTIMIZANDO RENDER GPU", current++ / totalSteps)
                runShellCommand("setprop debug.hwui.renderer skiagl")
                runShellCommand("setprop debug.egl.hw 1")

                if (Build.MANUFACTURER.contains("HONOR", ignoreCase = true)) {
                    onProgress("📱 OPTIMIZANDO MagicOS...", current / totalSteps)
                    listOf("com.hihonor.browserhomepage", "com.hihonor.android.totemweather").forEach { 
                        runShellCommand("pm disable-user --user 0 $it")
                    }
                }
                current++

                onProgress("⏳ AOT COMPILATION (PORCENTUAL)...", current++ / totalSteps)
                runAotCompilation(onProgress, 6f/totalSteps, 1f/totalSteps)

                onProgress("🔒 STEALTH MODE ACTIVADO", current / totalSteps)
                runShellCommand("settings put global development_settings_enabled 0")
                runShellCommand("sm fstrim")

                onProgress("✅ ACELERACIÓN COMPLETADA", 1f)
                Thread.sleep(800)
                onComplete()
            } catch (e: Exception) {
                onProgress("❌ ERROR CRÍTICO", 0f)
            }
        }.start()
    }

    fun executeRestoration(onProgress: (String, Float) -> Unit, onComplete: () -> Unit) {
        Thread {
            try {
                val total = 5f
                var step = 0f

                onProgress("🔄 RESTAURANDO ANIMACIONES", step++ / total)
                PHASE_ANIMATIONS_RESTORE.forEach { runShellCommand(it) }

                onProgress("🔄 RESTAURANDO DISPLAY", step++ / total)
                PHASE_DISPLAY_RESTORE.forEach { runShellCommand(it) }

                onProgress("🔄 RESTAURANDO ENERGÍA", step++ / total)
                PHASE_PERFORMANCE_RESTORE.forEach { runShellCommand(it) }

                onProgress("🔄 RESTAURANDO CACHÉ", step++ / total)
                PHASE_CACHE_RESTORE.forEach { runShellCommand(it) }

                onProgress("🔄 RESETEANDO STEALTH", step++ / total)
                PHASE_STEALTH_RESTORE.forEach { runShellCommand(it) }

                onProgress("✅ SISTEMA RESTAURADO", 1f)
                Thread.sleep(800)
                onComplete()
            } catch (e: Exception) {
                onProgress("❌ ERROR EN RESTAURACIÓN", 0f)
            }
        }.start()
    }

    private fun runAotCompilation(onProgress: (String, Float) -> Unit, startProgress: Float, weight: Float) {
        val packages = getInstalledPackages()
        val total = packages.size.coerceAtLeast(1)
        packages.forEachIndexed { index, pkg ->
            val p = startProgress + (index.toFloat() / total) * weight
            val percent = (index * 100) / total
            onProgress("⏳ AOT COMPILATION ($percent%): $pkg", p)
            runShellCommand("cmd package compile -m speed $pkg")
        }
    }

    private fun getInstalledPackages(): List<String> {
        val output = runShellCommandWithOutput("pm list packages")
        return output.lines().filter { it.startsWith("package:") }.map { it.substringAfter("package:").trim() }
    }

    private fun runShellCommand(cmd: String) {
        try {
            val process = newShizukuProcess(arrayOf("sh", "-c", cmd))
            process?.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "CMD Fail: $cmd", e)
        }
    }

    private fun runShellCommandWithOutput(cmd: String): String {
        return try {
            val process = newShizukuProcess(arrayOf("sh", "-c", cmd)) ?: return ""
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Professional Reflection Wrapper for Shizuku process creation.
     * Prevents issues with private/protected methods across different versions.
     */
    private fun newShizukuProcess(cmd: Array<String>): java.lang.Process? {
        return try {
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
            method.isAccessible = true
            method.invoke(null, cmd, null, null) as? java.lang.Process
        } catch (e: Exception) {
            Log.e(TAG, "Process Creation Failed", e)
            null
        }
    }
}
