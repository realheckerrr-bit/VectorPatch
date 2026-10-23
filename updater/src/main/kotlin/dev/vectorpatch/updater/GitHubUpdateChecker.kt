package dev.vectorpatch.updater

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Serializable
private data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
private data class GitHubAsset(
    val name: String = "",
    val browser_download_url: String = ""
)

class GitHubUpdateChecker(
    private val context: Context,
    private val owner: String,
    private val repo: String
) : UpdateChecker {

    private val client = OkHttpClient()
    private val json   = Json { ignoreUnknownKeys = true }
    private val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"

    override suspend fun getLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val request  = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body    = response.body?.string() ?: return@withContext null
            val release = json.decodeFromString<GitHubRelease>(body)

            val currentVersion = currentVersion()
            val latestVersion  = release.tag_name.removePrefix("v")
            val isNewer        = isVersionNewer(latestVersion, currentVersion)

            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }

            ReleaseInfo(
                tagName     = release.tag_name,
                name        = release.name,
                body        = release.body,
                downloadUrl = apkAsset?.browser_download_url ?: "",
                isNewer     = isNewer
            )
        } catch (_: IOException) { null }
    }

    override suspend fun triggerDownload() {
        // TODO: launch UpdateDownloadService
    }

    private fun currentVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    } catch (_: PackageManager.NameNotFoundException) { "0.0.0" }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts  = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
