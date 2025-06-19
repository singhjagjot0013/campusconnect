package com.integration.campusconnect

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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class RepliesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topicId = intent.getIntExtra("TOPIC_ID", -1)
        setContent {
            if (topicId != -1) {
                RepliesScreen(topicId)
            } else {
                Text("Invalid Topic")
            }
        }
    }
}

@Composable
fun RepliesScreen(topicId: Int) {
    val context = LocalContext.current
    var replies by remember { mutableStateOf(listOf<JSONObject>()) }
    var replyContent by remember { mutableStateOf("") }

    // Load replies
    LaunchedEffect(true) {
        val queue = Volley.newRequestQueue(context)
        val url = "${NetworkUtils.BASE_URL}/get_replies?topic_id=$topicId"
        val request = JsonArrayRequest(url,
            { response ->
                val list = mutableListOf<JSONObject>()
                for (i in 0 until response.length()) {
                    list.add(response.getJSONObject(i))
                }
                replies = list
            },
            { error ->
                Toast.makeText(context, "Error fetching replies", Toast.LENGTH_SHORT).show()
            })
        queue.add(request)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Replies", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(replies) { reply ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("By: ${reply.getString("author_email")}", style = MaterialTheme.typography.bodySmall)
                        Text(reply.getString("content"), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        OutlinedTextField(
            value = replyContent,
            onValueChange = { replyContent = it },
            label = { Text("Your Reply") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val queue = Volley.newRequestQueue(context)
                val url = "${NetworkUtils.BASE_URL}/post_reply"
                val jsonBody = JSONObject().apply {
                    put("topic_id", topicId)
                    put("author_email", "student@kpu.ca")  // TODO: Replace with actual user session email
                    put("content", replyContent)
                }

                val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    { response ->
                        Toast.makeText(context, "Reply posted", Toast.LENGTH_SHORT).show()
                        replyContent = ""
                    },
                    { error ->
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    })

                queue.add(request)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Post Reply")
        }
    }
}
