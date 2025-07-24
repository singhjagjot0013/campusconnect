package com.integration.campusconnect

import android.content.Intent
import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log
import androidx.core.content.edit

object NetworkUtils {

    const val BASE_URL = "http://3.21.247.142:5000"  // EC2 IP

    fun login(context: Context, email: String, password: String) {
        val queue = Volley.newRequestQueue(context)
        val url = "$BASE_URL/login"

        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                val sharedPref = context.getSharedPreferences("CampusConnectPrefs", Context.MODE_PRIVATE)
                sharedPref.edit {
                    putString("user_email", email)
                }

                Log.d("LoginDebug", "Saved user_email: $email")

                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                // 🚀 Redirect to MainActivity (NOT HomeActivity)
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            { error ->
                Toast.makeText(context, "Login Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

    fun offerRide(
        context: Context,
        driverEmail: String,
        origin: String,
        destination: String,
        date: String,
        time: String,
        seatsAvailable: Int
    ) {
        val url = "$BASE_URL/offer_ride"
        val queue = Volley.newRequestQueue(context)

        val requestBody = JSONObject().apply {
            put("driver_email", driverEmail)
            put("origin", origin)
            put("destination", destination)
            put("date", date)
            put("time", time)
            put("seats_available", seatsAvailable)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Toast.makeText(context, "Ride offered successfully!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            })

        queue.add(request)
    }
}
