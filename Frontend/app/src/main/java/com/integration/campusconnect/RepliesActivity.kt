package com.integration.campusconnect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
class RepliesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val topicId = intent.getIntExtra("TOPIC_ID", -1)
        setContent {
            CampusConnectTheme {
                if (topicId != -1) {
                    RepliesScreen(topicId)
                } else {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Text("Invalid Topic")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepliesScreen(topicId: Int) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
    val loggedInEmail = sharedPref.getString("user_email", "unknown@student.kpu.ca")

    var replies by remember { mutableStateOf(listOf<JSONObject>()) }
    var replyContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadReplies() {
        isLoading = true
        val url = "${NetworkUtils.BASE_URL}/get_replies/$topicId"
        val queue = Volley.newRequestQueue(context)

        val request = JsonArrayRequest(
            url,
            { response ->
                val list = mutableListOf<JSONObject>()
                for (i in 0 until response.length()) {
                    list.add(response.getJSONObject(i))
                }
                replies = list
                isLoading = false
            },
            { error ->
                Toast.makeText(context, "Error loading replies", Toast.LENGTH_SHORT).show()
                Log.e("REPLIES_DEBUG", "Load error: ${error.message}")
                isLoading = false
            })
        queue.add(request)
    }

    LaunchedEffect(Unit) { loadReplies() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replies", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = replyContent,
                    onValueChange = { if (it.length <= 1000) replyContent = it },
                    label = { Text("Your reply (max 1000 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (replyContent.isBlank()) {
                            Toast.makeText(context, "Reply cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val url = "${NetworkUtils.BASE_URL}/reply_topic"
                        val jsonBody = JSONObject().apply {
                            put("topic_id", topicId)
                            put("replier_email", loggedInEmail)
                            put("reply", replyContent)
                        }

                        val queue = Volley.newRequestQueue(context)
                        val request = JsonObjectRequest(
                            Request.Method.POST, url, jsonBody,
                            {
                                Toast.makeText(context, "Reply posted!", Toast.LENGTH_SHORT).show()
                                replyContent = ""
                                loadReplies()
                            },
                            {
                                Toast.makeText(context, "Failed to post reply", Toast.LENGTH_SHORT).show()
                                Log.e("REPLIES_DEBUG", "Post error: ${it.message}")
                            })
                        queue.add(request)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Post Reply", color = Color.White)
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Text("Loading replies...", style = MaterialTheme.typography.bodyMedium)
            } else if (replies.isEmpty()) {
                Text("No replies yet. Be the first to reply!", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(replies) { reply ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                // Avatar
                                val initials = reply.optString("replier_email")
                                    .split("@")[0]
                                    .split(".", "_", "-")
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .take(2)
                                    .joinToString("")

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        initials,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "By: ${reply.optString("replier_email")}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "At: ${reply.optString("created_at")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = reply.optString("reply"),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
