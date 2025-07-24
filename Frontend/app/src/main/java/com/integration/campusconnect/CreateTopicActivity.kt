package com.integration.campusconnect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.integration.campusconnect.ui.theme.CampusConnectTheme

class CreateTopicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CreateTopicScreen { finish() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTopicScreen(onTopicCreated: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val savedEmail = sharedPref.getString("user_email", "")

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var authorEmail by remember { mutableStateOf(savedEmail ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start a New Topic", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = authorEmail,
                onValueChange = { authorEmail = it },
                label = { Text("Your Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank() || authorEmail.isBlank()) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val url = "${NetworkUtils.BASE_URL}/create_topic"
                    val jsonBody = JSONObject().apply {
                        put("title", title)
                        put("content", content)
                        put("author_email", authorEmail)
                    }

                    val request = JsonObjectRequest(
                        Request.Method.POST, url, jsonBody,
                        { response ->
                            Toast.makeText(context, "Topic created!", Toast.LENGTH_SHORT).show()
                            onTopicCreated()
                        },
                        { error ->
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )

                    Volley.newRequestQueue(context).add(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit Topic", color = Color.White)
            }
        }
    }
}
