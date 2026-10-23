package dev.vectorpatch.service

import android.os.Process
import java.io.File
import kotlin.system.exitProcess

class VectorPatchUserService : IVectorPatchService.Stub() {

    override fun installApk(apkPath: String) {
        val process = ProcessBuilder("pm", "install", "-r", "-d", apkPath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("Package install failed with code $exitCode: $output")
        }
    }

    override fun uninstallPackage(packageName: String) {
        val process = ProcessBuilder("pm", "uninstall", packageName)
            .redirectErrorStream(true)
            .start()
        process.waitFor()
    }

    override fun isPackageInstalled(packageName: String): Boolean {
        val process = ProcessBuilder("pm", "path", packageName)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.isNotBlank() && output.startsWith("package:")
    }

    override fun getPatchableApps(): List<String> {
        val process = ProcessBuilder("pm", "list", "packages", "-3")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readLines()
        process.waitFor()
        return output.mapNotNull { line ->
            if (line.startsWith("package:")) line.removePrefix("package:").trim() else null
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}
