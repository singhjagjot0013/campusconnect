package com.integration.campusconnect
// AppDrawer.kt

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusConnectDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    content: @Composable (PaddingValues) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Campus Connect", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

                fun navTo(target: Class<*>) {
                    context.startActivity(Intent(context, target))
                    scope.launch { drawerState.close() }
                }

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navTo(HomeActivity::class.java) }
                )

                NavigationDrawerItem(
                    label = { Text("Find a Ride") },
                    selected = false,
                    onClick = { navTo(FindRideActivity::class.java) }
                )

                NavigationDrawerItem(
                    label = { Text("Offer a Ride") },
                    selected = false,
                    onClick = { navTo(OfferRideActivity::class.java) }
                )

                NavigationDrawerItem(
                    label = { Text("Discussion Forum") },
                    selected = false,
                    onClick = { navTo(DiscussionForumActivity::class.java) }
                )

                NavigationDrawerItem(
                    label = { Text("Login") },
                    selected = false,
                    onClick = { navTo(MainActivity::class.java) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Campus Connect") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            content = content
        )
    }
}

