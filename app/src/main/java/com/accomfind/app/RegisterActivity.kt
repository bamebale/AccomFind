package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.accomfind.app.data.AppDatabaseHelper
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etStudentId = findViewById<EditText>(R.id.etStudentId)
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val studentId = etStudentId.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (fullName.isEmpty() || studentId.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDatabaseHelper(this)
            val success = db.registerUser(username, password, fullName, studentId)

            if (success) {
                Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Username already taken. Please choose another.", Toast.LENGTH_SHORT).show()
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }
}
