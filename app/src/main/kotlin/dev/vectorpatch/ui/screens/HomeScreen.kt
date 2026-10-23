package dev.vectorpatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.vectorpatch.ui.navigation.Screen
import dev.vectorpatch.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    vm: HomeViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VectorPatch", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text("Non-root Xposed Framework",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Framework Status Card
            item {
                StatusCard(
                    title = "Framework Status",
                    status = uiState.frameworkActive,
                    activeText = "Active & Running",
                    inactiveText = "Not Active — Patch an app to begin"
                )
            }
            // Shizuku Status Card
            item {
                StatusCard(
                    title = "Shizuku",
                    status = uiState.shizukuRunning,
                    activeText = "Connected (silent installs enabled)",
                    inactiveText = "Not running — manual installs only",
                    warningMode = true
                )
            }
            // Root / Zygisk Status Card
            item {
                StatusCard(
                    title = "Root / Zygisk",
                    status = uiState.rootAvailable,
                    activeText = "Available (system-wide hooks enabled)",
                    inactiveText = "Not available — non-root mode active",
                    warningMode = true
                )
            }
            // Quick Action Buttons
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Apps.route) }
                    ) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Patch App")
                    }
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Modules.route) }
                    ) {
                        Icon(Icons.Default.Extension, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Modules")
                    }
                }
            }
            // Stats Card
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Patched Apps",  value = uiState.patchedAppCount.toString())
                        StatItem(label = "Active Modules", value = uiState.activeModuleCount.toString())
                        StatItem(label = "Hooks",          value = uiState.hookCount.toString())
                    }
                }
            }
            // Update banner
            if (uiState.updateAvailable) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        ListItem(
                            headlineContent = { Text("Update available: v${uiState.latestVersion}") },
                            supportingContent = { Text("Tap to update VectorPatch") },
                            leadingContent = { Icon(Icons.Default.SystemUpdate,
                                null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = {
                                FilledTonalButton(onClick = { vm.startUpdate() }) { Text("Update") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    status: Boolean,
    activeText: String,
    inactiveText: String,
    warningMode: Boolean = false
) {
    val icon = if (status) Icons.Outlined.CheckCircle else Icons.Outlined.Warning
    val tint  = when {
        status       -> MaterialTheme.colorScheme.primary
        warningMode  -> MaterialTheme.colorScheme.tertiary
        else         -> MaterialTheme.colorScheme.error
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent   = { Text(title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(if (status) activeText else inactiveText) },
            leadingContent    = { Icon(icon, null, tint = tint) }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
