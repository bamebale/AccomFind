package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accomfind.app.data.AppDatabaseHelper
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabaseHelper
    private lateinit var adapter: ListingAdapter
    private var filterMaxPrice: Double = 5000.0
    private var filterLocation: String? = null
    private var filterDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        db = AppDatabaseHelper(this)

        val rvListings = findViewById<RecyclerView>(R.id.rvListings)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnFilter = findViewById<MaterialButton>(R.id.btnFilter)

        // Bottom nav
        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Setup RecyclerView
        val allListings = db.getAllAccommodations().toMutableList()
        adapter = ListingAdapter(this, allListings)
        rvListings.layoutManager = LinearLayoutManager(this)
        rvListings.adapter = adapter

        // Search
        etSearch.doAfterTextChanged { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                adapter.updateData(db.getAllAccommodations())
            } else {
                adapter.updateData(db.searchAccommodations(query))
            }
        }

        // Filter button
        btnFilter.setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload with filters from session on resume
        val savedPrefs = SessionManager.hasSavedPrefs(this)
        if (savedPrefs) {
            filterMaxPrice = SessionManager.getFilterMaxPrice(this)
            filterLocation = SessionManager.getFilterLocation(this)
            filterDate = SessionManager.getFilterDate(this)
        }
        val filtered = db.getFilteredAccommodations(filterMaxPrice, filterLocation, filterDate)
        adapter.updateData(filtered)
    }
}
