package com.integration.campusconnect

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.integration.campusconnect.ui.theme.CampusConnectTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                HomeDashboard()
            }
        }
    }
}
@Preview
@Composable
fun HomeDashboard() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CampusConnectDrawer(drawerState, scope, context) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
//            Text("Welcome to Campus Connect", style = MaterialTheme.typography.headlineMedium)
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Button(
//                onClick = { context.startActivity(Intent(context, FindRideActivity::class.java)) },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Find a Ride")
//            }
//
//            Button(
//                onClick = { context.startActivity(Intent(context, OfferRideActivity::class.java)) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp)
//            ) {
//                Text("Offer a Ride")
//            }
//
//            Button(
//                onClick = { context.startActivity(Intent(context, DiscussionForumActivity::class.java)) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp)
//            ) {
//                Text("Discussion Forum")
//            }
        }
    }
}
