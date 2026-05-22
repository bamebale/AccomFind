package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accomfind.app.data.AppDatabaseHelper
import com.google.android.material.button.MaterialButton

class SavedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        val db = AppDatabaseHelper(this)
        val userId = SessionManager.getUserId(this)

        val rvSaved = findViewById<RecyclerView>(R.id.rvSaved)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        val savedItems = db.getSavedAccommodations(userId).toMutableList()
        val adapter = ListingAdapter(this, savedItems)
        rvSaved.layoutManager = LinearLayoutManager(this)
        rvSaved.adapter = adapter

        btnBack.setOnClickListener { finish() }
    }
}
