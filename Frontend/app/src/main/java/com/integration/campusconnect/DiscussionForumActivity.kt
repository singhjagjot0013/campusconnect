package com.integration.campusconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.integration.campusconnect.ui.theme.CampusConnectTheme

data class Topic(
    val id: Int,
    val title: String,
    val content: String,
    val author_email: String
)

class DiscussionTopicsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Check login session
        val sharedPref = getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", null)
        Log.d("SessionCheck", "Retrieved user_email: $userEmail")

        if (userEmail == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            CampusConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiscussionTopicsScreen()
                }
            }
        }
    }
}

@Composable
fun DiscussionTopicsScreen() {
    val context = LocalContext.current
    var topics by remember { mutableStateOf(listOf<Topic>()) }

    LaunchedEffect(true) {
        val queue = Volley.newRequestQueue(context)
        val url = "${NetworkUtils.BASE_URL}/get_topics"

        val request = JsonArrayRequest(url,
            { response ->
                val list = mutableListOf<Topic>()
                for (i in 0 until response.length()) {
                    val item = response.getJSONObject(i)
                    val topic = Topic(
                        id = item.getInt("id"),
                        title = item.getString("title"),
                        content = item.getString("content"),
                        author_email = item.getString("author_email")
                    )
                    list.add(topic)
                }
                topics = list
            },
            { error ->
                Toast.makeText(context, "Error fetching topics", Toast.LENGTH_SHORT).show()
            })
        queue.add(request)
    }

    Column(Modifier.padding(16.dp)) {
        Button(
            onClick = { context.startActivity(Intent(context, CreateTopicActivity::class.java)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Topic")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(topics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            val intent = Intent(context, RepliesActivity::class.java)
                            intent.putExtra("TOPIC_ID", topic.id)
                            context.startActivity(intent)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Title: ${topic.title}", style = MaterialTheme.typography.titleMedium)
                        Text("By: ${topic.author_email}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(topic.content, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
