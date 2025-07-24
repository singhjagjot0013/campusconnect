package com.integration.campusconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import org.json.JSONObject
import android.content.Context
import com.android.volley.Request

class ProfileSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProfileSettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val email = sharedPref.getString("user_email", "") ?: ""

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadProfile() {
        val url = "${NetworkUtils.BASE_URL}/get_profile/$email"
        val queue = Volley.newRequestQueue(context)

        val request = JsonObjectRequest(
            url, null,
            { response ->
                name = response.optString("name", "")
                phone = response.optString("phone", "")
                bio = response.optString("bio", "")
                isLoading = false
            },
            {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )

        queue.add(request)
    }

    fun updateProfile() {
        val url = "${NetworkUtils.BASE_URL}/update_profile"
        val queue = Volley.newRequestQueue(context)
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("name", name)
            put("phone", phone)
            put("bio", bio)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(context, "Update failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email (readonly)") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { updateProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Changes", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
