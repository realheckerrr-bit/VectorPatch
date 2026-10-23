package dev.vectorpatch.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ShizukuManager"
    }

    private var userService: IVectorPatchService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            userService = IVectorPatchService.Stub.asInterface(service)
            Log.d(TAG, "IVectorPatchService connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            Log.d(TAG, "IVectorPatchService disconnected")
        }
    }

    val isAvailable: Boolean
        get() = try { Shizuku.pingBinder() } catch (_: Throwable) { false }

    val hasPermission: Boolean
        get() = isAvailable && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    init {
        if (isAvailable) {
            bindService()
        }
        Shizuku.addBinderReceivedListenerSticky {
            bindService()
        }
        Shizuku.addBinderDeadListener {
            userService = null
        }
    }

    fun bindService() {
        if (!hasPermission) return
        try {
            val args = Shizuku.UserServiceArgs(
                ComponentName(context.packageName, VectorPatchUserService::class.java.name)
            ).daemon(false)
             .processNameSuffix("service")
             .debuggable(true)
             .version(1)

            Shizuku.bindUserService(args, serviceConnection)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to bind UserService: ${e.message}")
        }
    }

    fun requestPermission() {
        if (isAvailable && !hasPermission) {
            Shizuku.requestPermission(1001)
        }
    }

    /**
     * Install an APK via Shizuku (pm install) or fallback to intent-based install.
     */
    suspend fun installApk(apkFile: File) = withContext(Dispatchers.IO) {
        val service = userService
        if (service != null) {
            try {
                service.installApk(apkFile.absolutePath)
                Log.d(TAG, "Installed via VectorPatch UserService: ${apkFile.name}")
                return@withContext
            } catch (e: Exception) {
                Log.w(TAG, "UserService install failed, falling back: ${e.message}")
            }
        }
        // Fallback: direct process
        installViaAdbFallback(apkFile)
    }

    private fun installViaAdbFallback(apkFile: File) {
        val result = ProcessBuilder("pm", "install", "-r", apkFile.absolutePath)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText()
        Log.d(TAG, "ADB fallback install: $result")
    }

    suspend fun uninstallPackage(packageName: String) = withContext(Dispatchers.IO) {
        val service = userService
        if (service != null) {
            try {
                service.uninstallPackage(packageName)
                return@withContext
            } catch (e: Exception) {
                Log.w(TAG, "UserService uninstall failed: ${e.message}")
            }
        }
    }
}
