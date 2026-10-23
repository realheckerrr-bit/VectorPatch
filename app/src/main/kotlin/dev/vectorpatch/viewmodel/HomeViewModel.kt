package dev.vectorpatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vectorpatch.service.ShizukuManager
import dev.vectorpatch.updater.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import javax.inject.Inject

data class HomeUiState(
    val frameworkActive: Boolean   = false,
    val shizukuRunning: Boolean    = false,
    val rootAvailable: Boolean     = false,
    val patchedAppCount: Int       = 0,
    val activeModuleCount: Int     = 0,
    val hookCount: Int             = 0,
    val updateAvailable: Boolean   = false,
    val latestVersion: String      = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateChecker: UpdateChecker,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val shizukuRunning = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
            val rootAvailable  = checkRoot()

            _uiState.update { it.copy(
                shizukuRunning = shizukuRunning,
                rootAvailable  = rootAvailable,
                frameworkActive = shizukuRunning || rootAvailable
            ) }

            // Check for updates
            val update = updateChecker.getLatestRelease()
            if (update != null) {
                _uiState.update { it.copy(
                    updateAvailable = update.isNewer,
                    latestVersion   = update.tagName.removePrefix("v")
                ) }
            }
        }
    }

    fun startUpdate() {
        viewModelScope.launch {
            updateChecker.triggerDownload()
        }
    }

    private fun checkRoot(): Boolean = try {
        val p = Runtime.getRuntime().exec("su -c echo root")
        p.waitFor() == 0
    } catch (_: Exception) { false }
}
