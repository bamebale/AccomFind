package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class FilterActivity : AppCompatActivity() {

    private var selectedLocation: String? = null
    private val locationChipIds = mapOf(
        "Gaborone West" to R.id.chipGaboroneWest,
        "Block 8" to R.id.chipBlock8,
        "Tlokweng" to R.id.chipTlokweng,
        "Phase 2" to R.id.chipPhase2,
        "Phakalane" to R.id.chipPhakalane,
        "Broadhurst" to R.id.chipBroadhurst,
        "Block 6" to R.id.chipBlock6,
        "Kgale View" to R.id.chipKgaleView
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val sbPrice = findViewById<SeekBar>(R.id.sbPrice)
        val tvPriceValue = findViewById<TextView>(R.id.tvPriceValue)
        val etFilterDate = findViewById<EditText>(R.id.etFilterDate)
        val switchSavePrefs = findViewById<Switch>(R.id.switchSavePrefs)
        val btnApply = findViewById<MaterialButton>(R.id.btnApplyFilter)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        // Restore current session filter values
        sbPrice.max = 5000
        sbPrice.progress = SessionManager.getFilterMaxPrice(this).toInt()
        tvPriceValue.text = "Max: BWP ${String.format("%,d", sbPrice.progress)}"
        etFilterDate.setText(SessionManager.getFilterDate(this) ?: "")
        selectedLocation = SessionManager.getFilterLocation(this)
        updateChipUI()

        sbPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val display = if (progress < 300) 300 else progress
                tvPriceValue.text = "Max: BWP ${String.format("%,d", display)}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Location chips
        locationChipIds.forEach { (location, chipId) ->
            findViewById<TextView>(chipId).setOnClickListener {
                selectedLocation = if (selectedLocation == location) null else location
                updateChipUI()
            }
        }

        btnBack.setOnClickListener { finish() }

        btnApply.setOnClickListener {
            val maxPrice = maxOf(sbPrice.progress.toDouble(), 300.0)
            val date = etFilterDate.text.toString().trim().ifEmpty { null }
            val savePrefs = switchSavePrefs.isChecked

            if (savePrefs) {
                SessionManager.saveFilterPrefs(this, maxPrice, selectedLocation, date)
            }

            // Send notification if prefs saved
            if (savePrefs) {
                val db = com.accomfind.app.data.AppDatabaseHelper(this)
                val results = db.getFilteredAccommodations(maxPrice, selectedLocation, date)
                if (results.isNotEmpty()) {
                    NotificationHelper.sendMatchNotification(this, results.size, selectedLocation)
                }
            }

            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(resultIntent)
            finish()
        }
    }

    private fun updateChipUI() {
        locationChipIds.forEach { (location, chipId) ->
            val chip = findViewById<TextView>(chipId)
            if (location == selectedLocation) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
            }
        }
    }
}
