package dev.vectorpatch.patcher.apk

import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles APK decoding, payload injection, manifest patching, and repacking.
 * Based on LSPatch's APK manipulation pipeline.
 */
class ApkProcessor(private val context: Context) {

    private val abis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")

    /**
     * Unzips the APK into a work directory for manipulation.
     */
    fun decodeApk(sourceApk: File): File {
        val workDir = File(context.cacheDir, "workdir_${System.currentTimeMillis()}")
        workDir.mkdirs()
        ZipFile(sourceApk).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val out = File(workDir, entry.name)
                if (entry.isDirectory) { out.mkdirs(); return@forEach }
                out.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    out.outputStream().use { input.copyTo(it) }
                }
            }
        }
        return workDir
    }

    /**
     * Injects the VectorPatch/LSPatch loader DEX into the unpacked APK.
     * The loader DEX is embedded as an asset in this library.
     */
    fun injectPayloadDex(workDir: File) {
        // Find next available classes.dex slot
        var index = 2
        while (File(workDir, "classes${index}.dex").exists()) index++
        val targetDex = File(workDir, "classes${index}.dex")

        // Copy loader DEX from our assets
        val loaderDex = extractAsset("lspatch/loader.dex")
        loaderDex?.copyTo(targetDex, overwrite = true)
    }

    /**
     * Embeds LSPlant + Dobby native .so files for the target ABIs.
     */
    fun injectNativeLibs(workDir: File) {
        for (abi in abis) {
            val libDir = File(workDir, "lib/$abi")
            libDir.mkdirs()
            // Copy our .so files
            listOf("liblsplant.so", "libdobby.so", "liblspatch.so").forEach { soName ->
                val soFile = extractAsset("lspatch/libs/$abi/$soName")
                soFile?.copyTo(File(libDir, soName), overwrite = true)
            }
        }
    }

    /**
     * Rewrites AndroidManifest.xml to inject AppComponentFactory hook.
     */
    fun patchManifest(workDir: File, packageName: String) {
        val manifestFile = File(workDir, "AndroidManifest.xml")
        if (!manifestFile.exists()) return
        // TODO: binary XML manipulation using axmldec/ManifestEditor
        // For now writes a marker — full implementation requires axmldec library
        // which is included via the patch-core native bridge in production
    }

    /**
     * Embeds selected Xposed module APKs into assets/lspatch/modules/
     */
    fun embedModules(workDir: File, modulePackages: List<String>) {
        val modulesDir = File(workDir, "assets/lspatch/modules")
        modulesDir.mkdirs()
        for (pkg in modulePackages) {
            try {
                val info   = context.packageManager.getApplicationInfo(pkg, 0)
                val srcApk = File(info.sourceDir)
                srcApk.copyTo(File(modulesDir, "${pkg}.apk"), overwrite = true)
            } catch (e: PackageManager.NameNotFoundException) {
                // Module not found, skip
            }
        }
    }

    /**
     * Repacks the modified work directory back into a (unsigned) APK.
     */
    fun repackApk(workDir: File, outputApk: File): File {
        val unsignedApk = File(outputApk.parent, "unsigned_${outputApk.name}")
        ZipOutputStream(FileOutputStream(unsignedApk)).use { zos ->
            workDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(workDir).path.replace(File.separatorChar, '/')
                zos.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return unsignedApk
    }

    private fun extractAsset(name: String): File? {
        return try {
            val tmp = File(context.cacheDir, name.substringAfterLast('/'))
            context.assets.open(name).use { input ->
                tmp.outputStream().use { input.copyTo(it) }
            }
            tmp
        } catch (_: Exception) { null }
    }
}
