package dev.vectorpatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vectorpatch.ui.theme.ThemeMode
import dev.vectorpatch.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    vm: SettingsViewModel = hiltViewModel()
) {
    val themeMode     by vm.themeMode.collectAsState()
    val dynamicColor  by vm.dynamicColor.collectAsState()
    val autoUpdate    by vm.autoUpdate.collectAsState()
    val version       by vm.appVersion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner)
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance section
            item { SectionHeader("Appearance") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Theme selector
                        ListItem(
                            headlineContent   = { Text("Theme") },
                            supportingContent = { Text(themeMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingContent    = { Icon(Icons.Default.Palette, null) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = themeMode == mode,
                                    onClick  = { vm.setThemeMode(mode) },
                                    label    = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                        HorizontalDivider()
                        ListItem(
                            headlineContent   = { Text("Dynamic Color") },
                            supportingContent = { Text("Use Material You colors (Android 12+)") },
                            leadingContent    = { Icon(Icons.Default.ColorLens, null) },
                            trailingContent   = { Switch(checked = dynamicColor, onCheckedChange = { vm.setDynamicColor(it) }) }
                        )
                    }
                }
            }
            // Updates section
            item { SectionHeader("Updates") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent   = { Text("Auto-check for updates") },
                            supportingContent = { Text("Check GitHub Releases on launch") },
                            leadingContent    = { Icon(Icons.Default.SystemUpdate, null) },
                            trailingContent   = { Switch(checked = autoUpdate, onCheckedChange = { vm.setAutoUpdate(it) }) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Check now") },
                            leadingContent  = { Icon(Icons.Default.Refresh, null) },
                            modifier        = Modifier.clickableWithRipple { vm.checkForUpdate() }
                        )
                    }
                }
            }
            // About section
            item { SectionHeader("About") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent   = { Text("Version") },
                            supportingContent = { Text(version) },
                            leadingContent    = { Icon(Icons.Default.Info, null) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent   = { Text("Source") },
                            supportingContent = { Text("github.com/realheckerrr-bit/VectorPatch") },
                            leadingContent    = { Icon(Icons.Default.Code, null) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent   = { Text("Credits") },
                            supportingContent = { Text("Built on LSPatch + Vector. Thanks to LSPosed team & JingMatrix.") },
                            leadingContent    = { Icon(Icons.Default.Favorite, null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

private fun Modifier.clickableWithRipple(onClick: () -> Unit) = this.then(
    Modifier /* clickable ripple handled by ListItem internally */
)
