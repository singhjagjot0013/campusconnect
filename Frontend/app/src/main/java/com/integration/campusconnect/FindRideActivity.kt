package com.integration.campusconnect

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import org.json.JSONObject
import java.util.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRideScreen() {
    val context = LocalContext.current
    var origin by remember { mutableStateOf(TextFieldValue()) }
    var destination by remember { mutableStateOf(TextFieldValue()) }
    var date by remember { mutableStateOf(TextFieldValue()) }
    var availableRides by remember { mutableStateOf(listOf<JSONObject>()) }
    var joinedRides by remember { mutableStateOf(listOf<JSONObject>()) }

    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val userEmail = sharedPref.getString("user_email", "") ?: ""

    fun fetchJoinedRides() {
        val url = "${NetworkUtils.BASE_URL}/my_joined_rides?email=$userEmail"
        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val ridesArray = response.getJSONArray("joined_rides")
                val ridesList = mutableListOf<JSONObject>()
                for (i in 0 until ridesArray.length()) {
                    ridesList.add(ridesArray.getJSONObject(i))
                }
                joinedRides = ridesList
            },
            { Toast.makeText(context, "Failed to fetch joined rides", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find a Ride", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            DateField(date) { newDate ->
                date = TextFieldValue(newDate)
            }

            Button(
                onClick = {
                    val url = "${NetworkUtils.BASE_URL}/find_rides"
                    val jsonBody = JSONObject().apply {
                        put("origin", origin.text)
                        put("destination", destination.text)
                        put("date", date.text)
                    }
                    val queue = Volley.newRequestQueue(context)
                    val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        { response ->
                            val ridesArray = response.getJSONArray("rides")
                            val ridesList = mutableListOf<JSONObject>()
                            for (i in 0 until ridesArray.length()) {
                                ridesList.add(ridesArray.getJSONObject(i))
                            }
                            availableRides = ridesList
                            fetchJoinedRides()
                        },
                        { Toast.makeText(context, "Failed to fetch rides", Toast.LENGTH_SHORT).show() })
                    queue.add(request)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Search", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("Available Rides")
            RideList(rides = availableRides, isJoined = false, userEmail = userEmail, onAction = { fetchJoinedRides() })

            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("My Joined Rides")
            RideList(rides = joinedRides, isJoined = true, userEmail = userEmail, onAction = { fetchJoinedRides() })
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Divider(modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
fun RideList(
    rides: List<JSONObject>,
    isJoined: Boolean,
    userEmail: String,
    onAction: () -> Unit
) {
    val context = LocalContext.current

    if (rides.isEmpty()) {
        Text(if (isJoined) "You haven't joined any rides yet." else "No rides found.")
    } else {
        rides.forEach { ride ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Driver: ${ride.getString("driver_email")}", color = Color.White)
                    Text("Route: ${ride.getString("origin")} → ${ride.getString("destination")}", color = Color.White)
                    Text("Time: ${ride.getString("date")} @ ${ride.getString("time")}", color = Color.White)
                    if (!isJoined) Text("Seats: ${ride.getInt("seats_available")}", color = Color.White)
                    Spacer(Modifier.height(6.dp))

                    val buttonText = if (isJoined) "Cancel" else "Join"
                    val url = if (isJoined) "/cancel_ride" else "/join_ride"
                    val jsonBody = JSONObject().apply {
                        put("ride_id", if (isJoined) ride.getInt("ride_id") else ride.getInt("id"))
                        put("rider_email", userEmail)
                    }

                    Button(
                        onClick = {
                            val fullUrl = "${NetworkUtils.BASE_URL}$url"
                            val request = JsonObjectRequest(Request.Method.POST, fullUrl, jsonBody,
                                {
                                    Toast.makeText(context, "$buttonText successful", Toast.LENGTH_SHORT).show()
                                    onAction()
                                },
                                {
                                    Toast.makeText(context, "Failed to $buttonText ride", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Volley.newRequestQueue(context).add(request)
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(buttonText, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun DateField(date: TextFieldValue, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text("Date (optional)") },
        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        onDateSelected("%04d-%02d-%02d".format(year, month + 1, day))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    )
}
