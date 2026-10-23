package dev.vectorpatch.updater

interface UpdateChecker {
    suspend fun getLatestRelease(): ReleaseInfo?
    suspend fun triggerDownload()
}

data class ReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val downloadUrl: String,
    val isNewer: Boolean
)
