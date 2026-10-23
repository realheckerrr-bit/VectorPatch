package dev.vectorpatch.service

import android.content.pm.PackageManager
import android.util.Log
import dev.vectorpatch.service.IVectorPatchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuManager @Inject constructor() {

    companion object {
        private const val TAG = "ShizukuManager"
    }

    val isAvailable: Boolean
        get() = try { Shizuku.pingBinder() } catch (_: Throwable) { false }

    val hasPermission: Boolean
        get() = isAvailable && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestPermission() {
        if (isAvailable && !hasPermission) {
            Shizuku.requestPermission(1001)
        }
    }

    /**
     * Install an APK via Shizuku (pm install) or fallback to intent-based install.
     */
    suspend fun installApk(apkFile: File) = withContext(Dispatchers.IO) {
        if (hasPermission) {
            try {
                installViaShizuku(apkFile)
                return@withContext
            } catch (e: Exception) {
                Log.w(TAG, "Shizuku install failed, falling back: ${e.message}")
            }
        }
        // Fallback: use pm command via ProcessBuilder
        installViaAdbFallback(apkFile)
    }

    private fun installViaShizuku(apkFile: File) {
        // Use shell process through Shizuku to run pm install
        Shizuku.newProcess(
            arrayOf("pm", "install", "-r", "-d", apkFile.absolutePath),
            null, null
        ).also { process ->
            val exitCode = process.waitFor()
            val output   = process.inputStream.bufferedReader().readText()
            if (exitCode != 0) throw RuntimeException("pm install failed ($exitCode): $output")
            Log.d(TAG, "Installed via Shizuku: $output")
        }
    }

    private fun installViaAdbFallback(apkFile: File) {
        // Fallback: direct process (needs INSTALL_PACKAGES on some ROMs)
        val result = ProcessBuilder("pm", "install", "-r", apkFile.absolutePath)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText()
        Log.d(TAG, "ADB fallback install: $result")
    }

    suspend fun uninstallPackage(packageName: String) = withContext(Dispatchers.IO) {
        if (hasPermission) {
            Shizuku.newProcess(arrayOf("pm", "uninstall", packageName), null, null).waitFor()
        }
    }
}
