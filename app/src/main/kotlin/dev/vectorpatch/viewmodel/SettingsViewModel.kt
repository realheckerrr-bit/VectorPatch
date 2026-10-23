package dev.vectorpatch.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.vectorpatch.ui.theme.ThemeMode
import dev.vectorpatch.updater.UpdateChecker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    private val KEY_THEME         = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    private val KEY_AUTO_UPDATE   = booleanPreferencesKey("auto_update")

    val themeMode: StateFlow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val dynamicColor: StateFlow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DYNAMIC_COLOR] ?: true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val autoUpdate: StateFlow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_UPDATE] ?: true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val appVersion: StateFlow<String> = flow {
        val version = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: PackageManager.NameNotFoundException) { "unknown" }
        emit(version ?: "unknown")
    }.stateIn(viewModelScope, SharingStarted.Lazily, "...")

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }
    fun setAutoUpdate(enabled: Boolean) = viewModelScope.launch {
        context.dataStore.edit { it[KEY_AUTO_UPDATE] = enabled }
    }
    fun checkForUpdate() {
        viewModelScope.launch { updateChecker.getLatestRelease() }
    }
}
