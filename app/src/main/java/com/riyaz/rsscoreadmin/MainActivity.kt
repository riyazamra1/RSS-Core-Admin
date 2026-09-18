package com.riyaz.rsscoreadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyaz.rsscoreadmin.data.*
import com.riyaz.rsscoreadmin.domain.AdminRepository
import com.riyaz.rsscoreadmin.ui.navigation.AdminDestination
import com.riyaz.rsscoreadmin.ui.screens.*
import com.riyaz.rsscoreadmin.ui.theme.RssCoreAdminTheme
import kotlinx.coroutines.launch

private class AdminViewModel : ViewModel() {
    private val repository = AdminRepository(RssCoreApi())
    val config = EndpointConfiguration()
    var health by mutableStateOf<EndpointHealth?>(null); private set
    var status by mutableStateOf<CoreStatusResult?>(null); private set
    var projects by mutableStateOf<CoreProjectsResult?>(null); private set
    var checking by mutableStateOf(false); private set

    fun refresh() {
        if (checking) return
        viewModelScope.launch {
            checking = true
            health = repository.checkPrimary(config)
            if (health?.state == HealthState.HEALTHY) {
                status = repository.status(config.active)
                projects = repository.projects(config.active)
            } else {
                status = null
                projects = null
            }
            checking = false
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RssCoreAdminTheme { AdminApp() } }
    }
}

@Composable
private fun AdminApp(vm: AdminViewModel = viewModel()) {
    var selected by remember { mutableStateOf(AdminDestination.COMMAND_CENTER) }
    var showConnectivity by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS Core Admin") },
                actions = { IconButton(onClick = vm::refresh) { Icon(Icons.Default.Settings, "Refresh") } }
            )
        },
        bottomBar = {
            NavigationBar {
                val destinations = listOf(
                    AdminDestination.COMMAND_CENTER to Icons.Default.Dashboard,
                    AdminDestination.RAY to Icons.Default.Build,
                    AdminDestination.ECOSYSTEM to Icons.Default.Work,
                    AdminDestination.OPERATIONS to Icons.Default.Settings,
                    AdminDestination.SECURITY to Icons.Default.Security
                )
                destinations.forEach { (destination, icon) ->
                    NavigationBarItem(
                        selected = selected == destination,
                        onClick = { selected = destination; showConnectivity = false },
                        icon = { Icon(icon, destination.title) },
                        label = { Text(destination.shortTitle) }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
                when {
                    showConnectivity -> DomainConnectivityScreen(vm.health, vm::refresh)
                    selected == AdminDestination.COMMAND_CENTER -> CommandCenterScreen(vm.health, vm.status, vm.projects, vm::refresh) { showConnectivity = true }
                    else -> ModuleScreen(selected) { showConnectivity = true }
                }
            }
        }
    }
}