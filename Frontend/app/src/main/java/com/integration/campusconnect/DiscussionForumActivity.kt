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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme
import java.text.SimpleDateFormat
import java.util.*

data class ForumPost(val author: String, val message: String, val category: String, val timestamp: String)

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

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun DiscussionForumScreen() {
    val context = LocalContext.current

    var postContent by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Academic Help") }

    val categories = listOf("Academic Help", "Study Groups", "Social Events")

    var posts by remember {
        mutableStateOf(
            listOf(
                ForumPost("Prateek", "Anyone has notes for INFO 4190?", "Academic Help", "7:45 PM"),
                ForumPost("Parneet", "Study group this Friday?", "Academic Help", "7:30 PM"),
                ForumPost("Jagjot", "Carpooling from Richmond at 8 AM", "Study Groups", "7:20 PM")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Discussion Forum", style = MaterialTheme.typography.headlineMedium)
        Text("Select Category:", style = MaterialTheme.typography.titleSmall)
        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedCategory)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = postContent,
            onValueChange = { postContent = it },
            label = { Text("Write a new post...") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (postContent.text.isNotBlank()) {
                    val newPost = ForumPost("You", postContent.text, selectedCategory, getCurrentTime())
                    posts = posts + newPost
                    postContent = TextFieldValue("")
                    Toast.makeText(context, "Post submitted", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post")
        }


        Text("Posts", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(posts.reversed()) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${post.author} • ${post.category} • ${post.timestamp}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
