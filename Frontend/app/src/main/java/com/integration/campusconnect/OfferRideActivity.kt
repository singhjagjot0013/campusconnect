package com.integration.campusconnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val defaultEmail = sharedPref.getString("user_email", "") ?: ""

    var driverEmail by remember { mutableStateOf(TextFieldValue(defaultEmail)) }
    var origin by remember { mutableStateOf(TextFieldValue()) }
    var destination by remember { mutableStateOf(TextFieldValue()) }
    var date by remember { mutableStateOf(TextFieldValue()) }
    var time by remember { mutableStateOf(TextFieldValue()) }
    var seats by remember { mutableStateOf(TextFieldValue()) }

    val calendar = Calendar.getInstance()

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                date = TextFieldValue("%04d-%02d-%02d".format(year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                time = TextFieldValue("%02d:%02d:00".format(hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer a Ride", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = driverEmail,
                onValueChange = { driverEmail = it },
                label = { Text("Your Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origin") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker() },
                readOnly = true
            )
            OutlinedTextField(
                value = time,
                onValueChange = {},
                label = { Text("Time") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker() },
                readOnly = true
            )
            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Seats Available") },
                leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val queue = Volley.newRequestQueue(context)
                    val url = "${NetworkUtils.BASE_URL}/offer_ride"
                    val jsonBody = JSONObject().apply {
                        put("driver_email", driverEmail.text)
                        put("origin", origin.text)
                        put("destination", destination.text)
                        put("date", date.text)
                        put("time", time.text)
                        put("seats_available", seats.text.toIntOrNull() ?: 0)
                    }

                    val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        {
                            Toast.makeText(context, "Ride Offered!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        })
                    queue.add(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit Ride", color = Color.White)
            }
        }
    }
}
