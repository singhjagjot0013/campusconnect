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
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import org.json.JSONObject
import android.content.Intent

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

@Composable
fun OfferRideScreen() {
    val context = LocalContext.current
    var driverEmail by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") } // Format: YYYY-MM-DD
    var time by remember { mutableStateOf("") } // Format: HH:MM:SS
    var seats by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Offer a Ride", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = driverEmail,
            onValueChange = { driverEmail = it },
            label = { Text("Your Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = origin,
            onValueChange = { origin = it },
            label = { Text("Origin") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destination") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (HH:MM:SS)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = seats,
            onValueChange = { seats = it },
            label = { Text("Seats Available") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val queue = Volley.newRequestQueue(context)
                val url = "${NetworkUtils.BASE_URL}/offer_ride"
                val jsonBody = JSONObject().apply {
                    put("driver_email", driverEmail)
                    put("origin", origin)
                    put("destination", destination)
                    put("date", date)
                    put("time", time)
                    put("seats_available", seats.toIntOrNull() ?: 0)
                }

                val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    { response ->
                        Toast.makeText(context, "Ride Offered!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, HomeActivity::class.java))
                    },
                    { error ->
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    })

                queue.add(request)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Ride")
        }
    }
}
