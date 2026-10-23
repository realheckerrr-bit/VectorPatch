package dev.vectorpatch.viewmodel

import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppItem(
    val packageName: String,
    val name: String,
    val isPatched: Boolean
)

@HiltViewModel
class AppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _allApps     = MutableStateFlow<List<AppItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery          = _searchQuery.asStateFlow()

    val apps = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) apps
        else apps.filter { it.name.contains(query, ignoreCase = true)
            || it.packageName.contains(query, ignoreCase = true) }
    }.let { flow ->
        // Convert to StateFlow for easy collection
        val sf = MutableStateFlow<List<AppItem>>(emptyList())
        viewModelScope.launch { flow.collect { sf.value = it } }
        sf.asStateFlow()
    }

    init { loadApps() }

    private fun loadApps() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                context.packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                    .map { info ->
                        val label = context.packageManager.getApplicationLabel(info).toString()
                        val patched = info.metaData?.containsKey("lspatch") == true
                        AppItem(info.packageName, label, patched)
                    }
                    .sortedBy { it.name }
            }
            _allApps.value = list
        }
    }

    fun search(query: String) { _searchQuery.value = query }
}
