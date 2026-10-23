package dev.vectorpatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vectorpatch.viewmodel.ModulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    paddingValues: PaddingValues,
    vm: ModulesViewModel = hiltViewModel()
) {
    val modules by vm.modules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xposed Modules", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.openModuleInstallPicker() },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("Install Module") }
            )
        }
    ) { inner ->
        if (modules.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Extension, null,
                        modifier = Modifier.size(64.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No Xposed modules installed",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Install an Xposed module APK to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner)
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(modules, key = { it.packageName }) { module ->
                    ModuleCard(
                        name        = module.name,
                        packageName = module.packageName,
                        version     = module.version,
                        description = module.description,
                        enabled     = module.enabled,
                        onToggle    = { vm.toggleModule(module.packageName, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    name: String, packageName: String, version: String,
    description: String, enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent   = { Text(name, fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Column {
                    Text(packageName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (description.isNotBlank())
                        Text(description, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp))
                }
            },
            leadingContent  = { Icon(Icons.Default.Extension, null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingContent = { Switch(checked = enabled, onCheckedChange = onToggle) },
            overlineContent = { Text("v$version",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
    }
}
