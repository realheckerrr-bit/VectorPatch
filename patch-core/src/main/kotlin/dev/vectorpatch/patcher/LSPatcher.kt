package dev.vectorpatch.patcher

import android.content.Context
import android.content.pm.PackageManager
import dev.vectorpatch.patcher.apk.ApkProcessor
import dev.vectorpatch.patcher.signing.ApkSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class PatchMode { INTEGRATED, MANAGER }

/**
 * Core LSPatch engine adapted for VectorPatch.
 *
 * Ported from LSPosed/LSPatch — performs:
 * 1. APK extraction from installed package
 * 2. DEX injection (XposedBridge + loader)
 * 3. Native .so embedding (LSPlant)
 * 4. AndroidManifest rewrite (AppComponentFactory hook)
 * 5. Re-pack and re-sign
 */
class LSPatcher(private val context: Context) {

    private val apkProcessor = ApkProcessor(context)
    private val apkSigner    = ApkSigner(context)

    companion object {
        const val PAYLOAD_DEX     = "lspatch/loader.dex"
        const val PAYLOAD_MODULES = "lspatch/modules/"
        const val META_KEY        = "lspatch"
    }

    fun getOutputApk(packageName: String): File {
        val cacheDir = File(context.cacheDir, "patched")
        cacheDir.mkdirs()
        return File(cacheDir, "${packageName}_patched.apk")
    }

    /**
     * Main patching pipeline.
     * @param options  Patch configuration (package, mode, modules)
     * @param onProgress callback(step: String, progress: 0..1)
     */
    suspend fun patch(
        options: PatchOptions,
        onProgress: (String, Float) -> Unit
    ) = withContext(Dispatchers.IO) {

        val outputApk = getOutputApk(options.packageName)

        // Step 1: locate source APK
        onProgress("Locating source APK…", 0.05f)
        val sourceApk = getSourceApk(options.packageName)

        // Step 2: decode & parse
        onProgress("Decoding APK…", 0.15f)
        val workDir = apkProcessor.decodeApk(sourceApk)

        // Step 3: inject payload DEX
        onProgress("Injecting XposedBridge…", 0.35f)
        apkProcessor.injectPayloadDex(workDir)

        // Step 4: inject native libs (LSPlant .so)
        onProgress("Embedding native hooks…", 0.50f)
        apkProcessor.injectNativeLibs(workDir)

        // Step 5: rewrite manifest
        onProgress("Patching manifest…", 0.65f)
        apkProcessor.patchManifest(workDir, options.packageName)

        // Step 6: embed modules (integrated mode)
        if (options.mode == PatchMode.INTEGRATED && options.modulePackages.isNotEmpty()) {
            onProgress("Embedding ${options.modulePackages.size} module(s)…", 0.75f)
            apkProcessor.embedModules(workDir, options.modulePackages)
        }

        // Step 7: repack
        onProgress("Repacking APK…", 0.85f)
        val unsignedApk = apkProcessor.repackApk(workDir, outputApk)

        // Step 8: sign
        onProgress("Signing APK…", 0.95f)
        apkSigner.sign(unsignedApk, outputApk)

        onProgress("Done!", 1.0f)

        // Cleanup work dir
        workDir.deleteRecursively()
    }

    private fun getSourceApk(packageName: String): File {
        val info = context.packageManager.getApplicationInfo(packageName, 0)
        return File(info.sourceDir)
    }
}

data class PatchOptions(
    val packageName: String,
    val mode: PatchMode,
    val modulePackages: List<String> = emptyList()
)
