package com.integration.campusconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DiscussionForumActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiscussionForumScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionForumScreen() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var topics by remember { mutableStateOf(listOf<JSONObject>()) }
    var filteredTopics by remember { mutableStateOf(listOf<JSONObject>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    fun loadTopics() {
        isLoading = true
        val queue = Volley.newRequestQueue(context)
        val url = "${NetworkUtils.BASE_URL}/get_topics"
        val request = JsonObjectRequest(url, null, { response ->
            val array = response.getJSONArray("topics")
            val list = mutableListOf<JSONObject>()
            for (i in 0 until array.length()) {
                list.add(array.getJSONObject(i))
            }
            topics = list
            filteredTopics = list
            isLoading = false
        }, {
            Toast.makeText(context, "Error loading topics", Toast.LENGTH_SHORT).show()
            isLoading = false
        })
        queue.add(request)
    }

    LaunchedEffect(Unit) { loadTopics() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion Board", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, CreateTopicActivity::class.java))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Topic")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    filteredTopics = topics.filter { topic ->
                        topic.getString("title").contains(searchQuery, ignoreCase = true) ||
                                topic.getString("message").contains(searchQuery, ignoreCase = true)
                    }
                },
                label = { Text("Search topics...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Text("Loading topics...", style = MaterialTheme.typography.bodyMedium)
            } else if (filteredTopics.isEmpty()) {
                Text("No topics found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredTopics) { topic ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color = MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = topic.getString("author_email")
                                            .split("@")[0]
                                            .split(".", "_", "-")
                                            .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                                            .take(2),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topic.getString("title"),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = formatDate(topic.optString("created_at")),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = topic.getString("message"),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(context, RepliesActivity::class.java)
                                            intent.putExtra("TOPIC_ID", topic.getInt("id"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Filled.ArrowForward, contentDescription = "Reply")
                                        Spacer(Modifier.width(8.dp))
                                        Text("Reply")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())
        val date = parser.parse(raw)
        formatter.format(date ?: return raw)
    } catch (e: Exception) {
        raw
    }
}
