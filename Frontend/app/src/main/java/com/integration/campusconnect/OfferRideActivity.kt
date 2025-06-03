package com.integration.campusconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme

class OfferRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OfferRideScreen()
                }
            }
        }
    }
}
@Preview
@Composable
fun OfferRideScreen() {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var campus by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Offer a Ride", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showError = it.isBlank()
            },
            label = { Text("Your Name") },
            isError = showError,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = time,
            onValueChange = {
                time = it
                showError = it.isBlank()
            },
            label = { Text("Pickup Time (e.g., 9:00 AM)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = showError,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = campus,
            onValueChange = {
                campus = it
                showError = it.isBlank()
                },
            label = { Text("Destination Campus") },
            isError = showError,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cost,
            onValueChange = { cost = it },
            label = { Text("Cost (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Later: Submit to backend
                if(name.isBlank()){
                    showError = true
                } else{
                    Toast.makeText(context, "Ride Offered Successfully!", Toast.LENGTH_LONG).show()
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Offer Ride")
        }
    }
}
