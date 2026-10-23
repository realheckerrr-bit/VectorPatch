package dev.vectorpatch.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vectorpatch.viewmodel.AppsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    paddingValues: PaddingValues,
    onPatchApp: (String) -> Unit,
    vm: AppsViewModel = hiltViewModel()
) {
    val apps  by vm.apps.collectAsState()
    val query by vm.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title  = { Text("Apps", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                SearchBar(
                    query            = query,
                    onQueryChange    = { vm.search(it) },
                    onSearch         = { vm.search(it) },
                    active           = false,
                    onActiveChange   = {},
                    leadingIcon      = { Icon(Icons.Default.Search, null) },
                    placeholder      = { Text("Search apps…") },
                    modifier         = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {}
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner)
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (apps.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(apps, key = { it.packageName }) { app ->
                    AppItem(
                        name        = app.name,
                        packageName = app.packageName,
                        isPatched   = app.isPatched,
                        onClick     = { onPatchApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppItem(name: String, packageName: String, isPatched: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        ListItem(
            headlineContent   = { Text(name, fontWeight = FontWeight.Medium) },
            supportingContent = { Text(packageName, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingContent    = { Icon(Icons.Outlined.Android, null,
                tint = MaterialTheme.colorScheme.primary) },
            trailingContent   = {
                if (isPatched) {
                    AssistChip(
                        onClick = {},
                        label   = { Text("Patched") },
                        leadingIcon = { Icon(Icons.Filled.CheckCircle, null,
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.primary) }
                    )
                } else {
                    FilledTonalButton(onClick = onClick) { Text("Patch") }
                }
            }
        )
    }
}
