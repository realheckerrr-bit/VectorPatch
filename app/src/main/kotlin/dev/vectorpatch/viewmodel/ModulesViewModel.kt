package dev.vectorpatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class XposedModule(
    val packageName: String,
    val name: String,
    val version: String,
    val description: String,
    val enabled: Boolean
)

@HiltViewModel
class ModulesViewModel @Inject constructor() : ViewModel() {

    private val _modules = MutableStateFlow<List<XposedModule>>(emptyList())
    val modules = _modules.asStateFlow()

    init { loadModules() }

    private fun loadModules() {
        viewModelScope.launch {
            // TODO: integrate with Room DB / package manager query for meta-data/xposedmodule
            _modules.value = emptyList()
        }
    }

    fun toggleModule(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            _modules.value = _modules.value.map {
                if (it.packageName == packageName) it.copy(enabled = enabled) else it
            }
            // TODO: persist to Room DB
        }
    }

    fun openModuleInstallPicker() {
        // TODO: launch file picker for module APK
    }
}
