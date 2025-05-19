package com.integration.campusconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Go to MainActivity (or LoginActivity, if you add that later)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}