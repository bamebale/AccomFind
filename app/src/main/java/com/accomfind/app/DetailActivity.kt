package com.accomfind.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.accomfind.app.data.AppDatabaseHelper
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class DetailActivity : AppCompatActivity() {

    private var accomId: Int = -1
    private lateinit var db: AppDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = AppDatabaseHelper(this)
        accomId = intent.getIntExtra("ACCOM_ID", -1)

        if (accomId == -1) {
            finish()
            return
        }

        val item = db.getAccommodationById(accomId)
        if (item == null) {
            Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Views
        val ivDetail = findViewById<ImageView>(R.id.ivDetail)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvAvailDate = findViewById<TextView>(R.id.tvDetailAvailDate)
        val tvDeposit = findViewById<TextView>(R.id.tvDetailDeposit)
        val tvAmenities = findViewById<TextView>(R.id.tvDetailAmenities)
        val tvType = findViewById<TextView>(R.id.tvDetailType)
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val btnReserve = findViewById<MaterialButton>(R.id.btnReserve)
        val btnRoute = findViewById<MaterialButton>(R.id.btnRoute)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // Populate
        tvTitle.text = item.title
        tvLocation.text = "📍 ${item.location}  •  ${item.distance}"
        tvPrice.text = "BWP ${String.format("%,.0f", item.price)}/mo"
        tvAvailDate.text = "Avail. ${item.availabilityDate}"
        tvDeposit.text = "Deposit: BWP ${String.format("%,.0f", item.deposit)}"
        tvAmenities.text = item.amenities.split(",").joinToString("\n") { "• ${it.trim()}" }
        tvType.text = item.type
        tvStatus.text = item.status

        // Status badge
        when (item.status) {
            "Reserved" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
                btnReserve.isEnabled = false
                btnReserve.text = "Already Reserved"
                btnReserve.alpha = 0.5f
            }
            "Rented" -> {
                tvStatus.text = "Rented"
                tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
                btnReserve.isEnabled = false
                btnReserve.text = "Property Rented"
                btnReserve.alpha = 0.5f
            }
            else -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_available)
                btnReserve.isEnabled = true
            }
        }

        // Load image
        val resId = resources.getIdentifier(item.imageResName, "drawable", packageName)
        if (resId != 0) {
            Glide.with(this).load(resId).centerCrop().into(ivDetail)
        }

        btnBack.setOnClickListener { finish() }

        // Save toggle
        val userId = SessionManager.getUserId(this)
        var isSaved = db.isAccommodationSaved(userId, accomId)
        updateSaveButtonState(btnSave, isSaved)

        btnSave.setOnClickListener {
            if (isSaved) {
                db.unsaveAccommodation(userId, accomId)
                isSaved = false
                Toast.makeText(this, "Removed from saved", Toast.LENGTH_SHORT).show()
            } else {
                db.saveAccommodation(userId, accomId)
                isSaved = true
                Toast.makeText(this, "Saved to your list", Toast.LENGTH_SHORT).show()
            }
            updateSaveButtonState(btnSave, isSaved)
        }

        // Reserve & Pay Deposit
        btnReserve.setOnClickListener {
            if (item.status != "Available") {
                Toast.makeText(this, "This room is no longer available.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("ACCOM_ID", accomId)
            startActivity(intent)
        }

        // Chat with Landlord
        findViewById<MaterialButton>(R.id.btnChat).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ACCOM_ID", accomId)
            startActivity(intent)
        }

        // Campus Route Navigation
        btnRoute.setOnClickListener {
            // Navigate to University of Botswana
            val ubLat = -24.6552
            val ubLng = 25.9143
            val gmmIntentUri = Uri.parse("geo:$ubLat,$ubLng?q=$ubLat,$ubLng(University+of+Botswana)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback to browser
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$ubLat,$ubLng&destination_place_id=University+of+Botswana")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh status
        val item = db.getAccommodationById(accomId) ?: return
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val btnReserve = findViewById<MaterialButton>(R.id.btnReserve)
        tvStatus.text = item.status
        when (item.status) {
            "Reserved" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
                btnReserve.isEnabled = false
                btnReserve.text = "Already Reserved"
                btnReserve.alpha = 0.5f
            }
            "Rented" -> {
                tvStatus.text = "Rented"
                tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
                btnReserve.isEnabled = false
                btnReserve.text = "Property Rented"
                btnReserve.alpha = 0.5f
            }
            else -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_available)
                btnReserve.isEnabled = true
                btnReserve.text = "Reserve & Pay Deposit"
                btnReserve.alpha = 1.0f
            }
        }
    }
    private fun updateSaveButtonState(btn: MaterialButton, saved: Boolean) {
        if (saved) {
            btn.text = "♥"
            btn.setTextColor(resources.getColor(R.color.status_reserved)) // Red
        } else {
            btn.text = "♡"
            btn.setTextColor(resources.getColor(R.color.white))
        }
    }
}
