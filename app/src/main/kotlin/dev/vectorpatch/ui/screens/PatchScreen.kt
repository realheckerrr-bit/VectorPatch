package dev.vectorpatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vectorpatch.patcher.PatchMode
import dev.vectorpatch.viewmodel.PatchViewModel
import dev.vectorpatch.viewmodel.PatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    vm: PatchViewModel = hiltViewModel()
) {
    LaunchedEffect(packageName) { vm.loadApp(packageName) }

    val uiState     by vm.uiState.collectAsState()
    val patchState  by vm.patchState.collectAsState()
    val modules     by vm.availableModules.collectAsState()
    val selectedMods by vm.selectedModules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Patch App", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App info
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(uiState.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(packageName, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Version: ${uiState.versionName}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Patch mode selector
            item {
                Text("Patch Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = uiState.patchMode == PatchMode.INTEGRATED,
                        onClick  = { vm.setPatchMode(PatchMode.INTEGRATED) },
                        shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Integrated") }
                    SegmentedButton(
                        selected = uiState.patchMode == PatchMode.MANAGER,
                        onClick  = { vm.setPatchMode(PatchMode.MANAGER) },
                        shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Manager") }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (uiState.patchMode == PatchMode.INTEGRATED)
                        "Modules baked into APK — standalone, no manager needed"
                    else
                        "Lightweight loader — manage modules dynamically via this app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Module selection (integrated mode only)
            if (uiState.patchMode == PatchMode.INTEGRATED && modules.isNotEmpty()) {
                item {
                    Text("Select Modules to Embed", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                items(modules, key = { it.packageName }) { mod ->
                    val checked = mod.packageName in selectedMods
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent   = { Text(mod.name) },
                            supportingContent = { Text("v${mod.version}") },
                            trailingContent   = {
                                Checkbox(checked = checked,
                                    onCheckedChange = { vm.toggleModuleSelection(mod.packageName) })
                            }
                        )
                    }
                }
            }

            // Patch progress / status
            item {
                when (patchState) {
                    is PatchState.Idle -> {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick  = { vm.startPatch() }
                        ) {
                            Text("Start Patching", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    is PatchState.InProgress -> {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text((patchState as PatchState.InProgress).step,
                                    style = MaterialTheme.typography.bodyMedium)
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                                    progress = { (patchState as PatchState.InProgress).progress })
                            }
                        }
                    }
                    is PatchState.Success -> {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent   = { Text("Patching complete!", fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("APK installed successfully") },
                                leadingContent    = { Icon(Icons.Filled.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) {
                            Text("Done")
                        }
                    }
                    is PatchState.Error -> {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent   = { Text("Patching failed", fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text((patchState as PatchState.Error).message) },
                                leadingContent    = { Icon(Icons.Filled.Error, null,
                                    tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { vm.reset() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
