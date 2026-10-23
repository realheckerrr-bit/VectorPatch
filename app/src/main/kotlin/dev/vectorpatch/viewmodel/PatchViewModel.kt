package dev.vectorpatch.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import dev.vectorpatch.patcher.LSPatcher
import dev.vectorpatch.patcher.PatchOptions
import dev.vectorpatch.patcher.PatchMode
import dev.vectorpatch.service.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PatchUiState(
    val appName: String       = "",
    val versionName: String   = "",
    val patchMode: PatchMode  = PatchMode.INTEGRATED
)

sealed class PatchState {
    object Idle : PatchState()
    data class InProgress(val step: String, val progress: Float) : PatchState()
    object Success : PatchState()
    data class Error(val message: String) : PatchState()
}

@HiltViewModel
class PatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lsPatcher: LSPatcher,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _uiState          = MutableStateFlow(PatchUiState())
    val uiState                   = _uiState.asStateFlow()
    private val _patchState       = MutableStateFlow<PatchState>(PatchState.Idle)
    val patchState                = _patchState.asStateFlow()
    private val _availableModules = MutableStateFlow<List<XposedModule>>(emptyList())
    val availableModules          = _availableModules.asStateFlow()
    private val _selectedModules  = MutableStateFlow<Set<String>>(emptySet())
    val selectedModules           = _selectedModules.asStateFlow()

    private var targetPackageName = ""

    fun loadApp(packageName: String) {
        targetPackageName = packageName
        viewModelScope.launch {
            try {
                val info = context.packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val pkgInfo = context.packageManager.getPackageInfo(packageName, 0)
                val label   = context.packageManager.getApplicationLabel(info).toString()
                _uiState.update { it.copy(appName = label, versionName = pkgInfo.versionName ?: "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(appName = packageName) }
            }
        }
    }

    fun setPatchMode(mode: PatchMode) { _uiState.update { it.copy(patchMode = mode) } }

    fun toggleModuleSelection(packageName: String) {
        _selectedModules.update { current ->
            if (packageName in current) current - packageName else current + packageName
        }
    }

    fun startPatch() {
        viewModelScope.launch {
            _patchState.value = PatchState.InProgress("Preparing APK…", 0.0f)
            try {
                withContext(Dispatchers.IO) {
                    val options = PatchOptions(
                        packageName  = targetPackageName,
                        mode         = _uiState.value.patchMode,
                        modulePackages = _selectedModules.value.toList()
                    )
                    lsPatcher.patch(options) { step, progress ->
                        _patchState.value = PatchState.InProgress(step, progress)
                    }
                    // Install via Shizuku or fallback
                    val outputApk = lsPatcher.getOutputApk(targetPackageName)
                    shizukuManager.installApk(outputApk)
                }
                _patchState.value = PatchState.Success
            } catch (e: Exception) {
                _patchState.value = PatchState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset() { _patchState.value = PatchState.Idle }
}
