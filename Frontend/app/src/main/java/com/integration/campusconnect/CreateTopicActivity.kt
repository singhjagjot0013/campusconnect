package com.integration.campusconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.Request
import org.json.JSONObject

class CreateTopicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateTopicScreen()
        }
    }
}

@Composable
fun CreateTopicScreen() {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var authorEmail by remember { mutableStateOf("") }

    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Start a New Topic", style = MaterialTheme.typography.headlineMedium)

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
                val queue = Volley.newRequestQueue(context)
                val url = "${NetworkUtils.BASE_URL}/create_topic"

                val jsonBody = JSONObject().apply {
                    put("title", title)
                    put("content", content)
                    put("author_email", authorEmail)
                }

                val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    { response ->
                        Toast.makeText(context, "Topic created!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, DiscussionTopicsActivity::class.java))
                    },
                    { error ->
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    })

                queue.add(request)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Topic")
        }
    }
}
