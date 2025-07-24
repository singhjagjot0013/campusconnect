package com.integration.campusconnect

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    HomeDashboard()
                }
            }
        }
    }
}

@Composable
fun HomeDashboard() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("CampusConnectPrefs", android.content.Context.MODE_PRIVATE)
    val userEmail = sharedPref.getString("user_email", "Guest")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $userEmail",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val buttonModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)

        Button(
            onClick = { context.startActivity(Intent(context, FindRideActivity::class.java)) },
            modifier = buttonModifier
        ) {
            Text("Find a Ride")
        }

        Button(
            onClick = { context.startActivity(Intent(context, OfferRideActivity::class.java)) },
            modifier = buttonModifier
        ) {
            Text("Offer a Ride")
        }

        Button(
            onClick = { context.startActivity(Intent(context, DiscussionForumActivity::class.java)) },
            modifier = buttonModifier
        ) {
            Text("Discussion Forum")
        }

        Button(
            onClick = { context.startActivity(Intent(context, ProfileSettingsActivity::class.java)) },
            modifier = buttonModifier
        ) {
            Text("Profile Settings")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                sharedPref.edit().clear().apply()
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, MainActivity::class.java))
            },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.onError)
        }
    }
}
