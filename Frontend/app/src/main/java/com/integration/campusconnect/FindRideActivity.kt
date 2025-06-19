package com.integration.campusconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.Request
import org.json.JSONObject

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

@Composable
fun FindRideScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val loggedInEmail = sharedPref.getString("user_email", null)

    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var rideResults by remember { mutableStateOf(listOf<JSONObject>()) }
    var joinedRides by remember { mutableStateOf(listOf<JSONObject>()) }

    fun fetchJoinedRides() {
        if (loggedInEmail == null) return
        val url = "${NetworkUtils.BASE_URL}/my_joined_rides?email=$loggedInEmail"
        val queue = Volley.newRequestQueue(context)

        val request = JsonObjectRequest(url, { response ->
            val ridesArray = response.getJSONArray("joined_rides")
            val list = mutableListOf<JSONObject>()
            for (i in 0 until ridesArray.length()) {
                list.add(ridesArray.getJSONObject(i))
            }
            joinedRides = list
        }, { error ->
            Toast.makeText(context, "Error loading joined rides", Toast.LENGTH_SHORT).show()
        })
        queue.add(request)
    }

    LaunchedEffect(Unit) {
        fetchJoinedRides()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("Find a Ride", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = origin, onValueChange = { origin = it }, label = { Text("Origin") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (optional)") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            val queue = Volley.newRequestQueue(context)
            val url = buildString {
                append("${NetworkUtils.BASE_URL}/find_rides?origin=${origin.trim()}&destination=${destination.trim()}")
                if (date.isNotBlank()) append("&date=${date.trim()}")
            }
            val request = JsonObjectRequest(url, { response ->
                val array = response.getJSONArray("rides")
                val results = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    results.add(array.getJSONObject(i))
                }
                rideResults = results
            }, { error ->
                Toast.makeText(context, "Error finding rides", Toast.LENGTH_SHORT).show()
            })
            queue.add(request)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Search")
        }

        Divider(thickness = 1.dp)
        Text("Available Rides", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(rideResults) { ride ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${ride.getString("origin")} to ${ride.getString("destination")} on ${ride.getString("date")} at ${ride.getString("time")}")
                        Button(onClick = {
                            val queue = Volley.newRequestQueue(context)
                            val joinUrl = "${NetworkUtils.BASE_URL}/join_ride"
                            val json = JSONObject().apply {
                                put("ride_id", ride.getInt("id"))
                                put("rider_email", loggedInEmail)
                            }
                            val request = JsonObjectRequest(Request.Method.POST, joinUrl, json, {
                                Toast.makeText(context, "Ride Joined", Toast.LENGTH_SHORT).show()
                                fetchJoinedRides()
                            }, {
                                Toast.makeText(context, "Join failed", Toast.LENGTH_SHORT).show()
                            })
                            queue.add(request)
                        }) {
                            Text("Join Ride")
                        }
                    }
                }
            }
        }

        Divider(thickness = 1.dp)
        Text("My Joined Rides", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(joinedRides) { ride ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${ride.getString("origin")} to ${ride.getString("destination")} on ${ride.getString("date")} at ${ride.getString("time")}")
                        Button(onClick = {
                            val queue = Volley.newRequestQueue(context)
                            val cancelUrl = "${NetworkUtils.BASE_URL}/cancel_ride"
                            val json = JSONObject().apply {
                                put("ride_id", ride.getInt("ride_id"))
                                put("rider_email", loggedInEmail)
                            }
                            val request = JsonObjectRequest(Request.Method.POST, cancelUrl, json, {
                                Toast.makeText(context, "Ride Cancelled", Toast.LENGTH_SHORT).show()
                                fetchJoinedRides()
                            }, {
                                Toast.makeText(context, "Cancel failed", Toast.LENGTH_SHORT).show()
                            })
                            queue.add(request)
                        }) {
                            Text("Cancel Ride")
                        }
                    }
                }
            }
        }
    }
}
