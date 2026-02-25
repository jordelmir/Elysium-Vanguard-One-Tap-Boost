package com.elysium.vanguard.boost.core

import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object OptimizationEngine {

    const val TAG = "ElysiumEngine"

    private val PHASE_ANIMATIONS = listOf(
        "settings put global window_animation_scale 0.15",
        "settings put global transition_animation_scale 0.15",
        "settings put global animator_duration_scale 0.15"
    )
    private val PHASE_DISPLAY = listOf(
        "settings put secure min_refresh_rate 120.0",
        "settings put secure peak_refresh_rate 120.0",
        "settings put system min_refresh_rate 120.0",
        "settings put system peak_refresh_rate 120.0",
        "settings put global high_refresh_rate_blacklist ''",
        "settings put system pointer_speed 7"
    )
    private val PHASE_PERFORMANCE = listOf(
        "cmd power set-fixed-performance-mode-enabled true",
        "settings put global low_power 0",
        "settings put global adaptive_battery_management_enabled 0",
        "settings put global adaptive_battery_management 0",
        "settings put global app_standby_enabled 0"
    )
    private val PHASE_CACHE = listOf(
        "settings put global activity_manager_constants max_cached_processes=64"
    )
    private val PHASE_GPU = listOf(
        "setprop debug.hwui.renderer skiagl",
        "setprop debug.egl.hw 1",
        "logcat -G 16M"
    )
    private val PHASE_STEALTH = listOf(
        "settings put global development_settings_enabled 0",
        "settings put global adb_enabled 1"
    )
    private val HONOR_DEBLOAT = listOf(
        "com.hihonor.browserhomepage", "com.hihonor.android.totemweather",
        "com.hihonor.searchservice", "com.hihonor.awareness",
        "com.hihonor.msdp", "com.hihonor.visionengine", "com.hihonor.magicvoice"
    )

    /**
     * @param onProgress  (message, progressFloat 0..1)
     */
    fun executeHyperAcceleration(
        onProgress: (String, Float) -> Unit,
        onComplete: () -> Unit
    ) {
        Thread {
            try {
                val totalPhases = 8f // animation, display, perf, cache, gpu, debloat, aot, stealth+trim
                var phase = 0f

                // Phase 1: Animations
                onProgress("⚡ ANIMACIONES 0.15x", phase / totalPhases)
                PHASE_ANIMATIONS.forEach { runShellCommand(it) }
                phase++

                // Phase 2: Display
                onProgress("📺 DISPLAY MAX Hz + TÁCTIL", phase / totalPhases)
                PHASE_DISPLAY.forEach { runShellCommand(it) }
                phase++

                // Phase 3: Performance
                onProgress("🔥 CPU/GPU AL 100%", phase / totalPhases)
                PHASE_PERFORMANCE.forEach { runShellCommand(it) }
                phase++

                // Phase 4: Cache
                onProgress("💾 CACHÉ x64 PROCESOS", phase / totalPhases)
                PHASE_CACHE.forEach { runShellCommand(it) }
                phase++

                // Phase 5: GPU
                onProgress("🎮 GPU SKIAGL", phase / totalPhases)
                PHASE_GPU.forEach { runShellCommand(it) }
                phase++

                // Phase 6: Debloat (conditional)
                if (Build.MANUFACTURER.contains("HONOR", ignoreCase = true)) {
                    onProgress("📱 OPTIMIZANDO MagicOS...", phase / totalPhases)
                    HONOR_DEBLOAT.forEach { runShellCommand("pm disable-user --user 0 $it") }
                }
                phase++

                // Phase 7: AOT — REAL per-package progress
                onProgress("⏳ AOT: Listando paquetes...", phase / totalPhases)
                val packages = getInstalledPackages()
                val totalPkgs = packages.size.coerceAtLeast(1)
                packages.forEachIndexed { index, pkg ->
                    val aotBase = phase / totalPhases
                    val aotProgress = aotBase + ((index + 1).toFloat() / totalPkgs) * (1f / totalPhases)
                    val percent = ((index + 1) * 100) / totalPkgs
                    onProgress("⏳ AOT: $percent% — ${pkg.takeLast(30)}", aotProgress)
                    runShellCommand("cmd package compile -m speed $pkg")
                }
                phase++

                // Phase 8: FSTRIM + Stealth
                onProgress("💿 FSTRIM + STEALTH MODE", phase / totalPhases)
                runShellCommand("sm fstrim")
                PHASE_STEALTH.forEach { runShellCommand(it) }

                onProgress("✅ PROTOCOLO COMPLETADO", 1f)
                Thread.sleep(500)
                onComplete()

            } catch (e: Exception) {
                Log.e(TAG, "Error", e)
                onProgress("ERROR: ${e.localizedMessage}", 0f)
            }
        }.start()
    }

    /**
     * Lists all installed packages via `pm list packages`
     * Returns a real count per-device for accurate AOT progress
     */
    private fun getInstalledPackages(): List<String> {
        return try {
            val output = runShellCommandWithOutput("pm list packages")
            output.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list packages: ${e.message}")
            emptyList()
        }
    }

    private fun runShellCommandWithOutput(cmd: String): String {
        return try {
            val shizukuClass = rikka.shizuku.Shizuku::class.java
            val method = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", cmd), null, null)

            val getInputStream = process.javaClass.getMethod("getInputStream")
            val inputStream = getInputStream.invoke(process) as java.io.InputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val result = reader.readText()
            reader.close()

            val waitFor = process.javaClass.getMethod("waitFor")
            waitFor.invoke(process)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Shell output error: ${e.message}")
            ""
        }
    }

    private fun runShellCommand(cmd: String) {
        try {
            val shizukuClass = rikka.shizuku.Shizuku::class.java
            val method = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", cmd), null, null)
            val waitFor = process.javaClass.getMethod("waitFor")
            waitFor.invoke(process)
        } catch (e: Exception) {
            Log.e(TAG, "Shell error: ${e.message}")
        }
    }
}
