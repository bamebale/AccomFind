package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accomfind.app.data.AppDatabaseHelper
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val db = AppDatabaseHelper(this)
        val userId = SessionManager.getUserId(this)

        // Fill profile info
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvStudentId = findViewById<TextView>(R.id.tvProfileStudentId)
        val tvPrefs = findViewById<TextView>(R.id.tvProfilePrefs)
        val rvReservations = findViewById<RecyclerView>(R.id.rvReservations)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        tvName.text = SessionManager.getFullName(this).ifEmpty { SessionManager.getUsername(this) }
        tvStudentId.text = "Student ID: ${SessionManager.getStudentId(this)}"

        // Saved filter preferences
        val maxPrice = SessionManager.getFilterMaxPrice(this)
        val location = SessionManager.getFilterLocation(this) ?: "Any"
        val date = SessionManager.getFilterDate(this) ?: "Any"
        tvPrefs.text = "Max Price: BWP ${String.format("%,.0f", maxPrice)}\nLocation: $location\nAvailable From: $date"

        // Reservations
        val reservedItems = db.getUserReservations(userId).toMutableList()
        val adapter = ListingAdapter(this, reservedItems)
        rvReservations.layoutManager = LinearLayoutManager(this)
        rvReservations.isNestedScrollingEnabled = false
        rvReservations.adapter = adapter

        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
