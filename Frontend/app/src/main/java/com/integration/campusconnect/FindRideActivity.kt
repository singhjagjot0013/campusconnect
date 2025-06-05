package com.integration.campusconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme

data class Ride(
    val name: String,
    val time: String,
    val campus: String,
    val price: String
)

class FindRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FindRideScreen()
                }
            }
        }
    }
}
@Preview
@Composable
fun FindRideScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val mockRides = listOf(
        Ride("Prateek C.", "9:00 AM", "Surrey Campus", "$5.00"),
        Ride("Inderdeep S.", "3:30 PM", "Richmond Campus", "Free"),
        Ride("Parneet K.", "6:00 PM", "Langley Campus", "$6.50")
    )
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CampusConnectDrawer(drawerState, scope, context) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Find a Ride", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search campus or time") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Map Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Map Placeholder", style = MaterialTheme.typography.bodyMedium)
        }

        Text("Available Rides", style = MaterialTheme.typography.titleMedium)

        mockRides.forEach { ride ->
            RideCard(
                name = ride.name,
                time = ride.time,
                campus = ride.campus,
                price = ride.price
            )
        }

        Button(
            onClick = { /* You can implement ride request logic later */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Request a Ride")
        }
    }
}
}
}

@Composable
fun RideCard(name: String, time: String, campus: String, price: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$name – $time", style = MaterialTheme.typography.bodyLarge)
            Text(campus, style = MaterialTheme.typography.bodyMedium)
            Text("Cost: $price", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
