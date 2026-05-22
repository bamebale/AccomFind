package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.accomfind.app.data.AppDatabaseHelper
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private var accomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        accomId = intent.getIntExtra("ACCOM_ID", -1)
        if (accomId == -1) { finish(); return }

        val db = AppDatabaseHelper(this)
        val item = db.getAccommodationById(accomId) ?: run { finish(); return }

        // Populate booking summary
        findViewById<TextView>(R.id.tvPayTitle).text = item.title
        findViewById<TextView>(R.id.tvPayLocation).text = "📍 ${item.location}"
        findViewById<TextView>(R.id.tvPayRent).text = "BWP ${String.format("%,.0f", item.price)}/mo"
        findViewById<TextView>(R.id.tvPayDeposit).text = "BWP ${String.format("%,.0f", item.deposit)}"

        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val btnConfirm = findViewById<MaterialButton>(R.id.btnConfirmPayment)
        val etCardName = findViewById<EditText>(R.id.etCardName)
        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry = findViewById<EditText>(R.id.etCardExpiry)
        val etCvv = findViewById<EditText>(R.id.etCardCvv)

        btnBack.setOnClickListener { finish() }

        btnConfirm.setOnClickListener {
            val name = etCardName.text.toString().trim()
            val number = etCardNumber.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            // Basic validation
            when {
                name.isEmpty() -> { Toast.makeText(this, "Enter cardholder name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                number.length != 16 -> { Toast.makeText(this, "Card number must be 16 digits", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                expiry.isEmpty() -> { Toast.makeText(this, "Enter expiry date", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                cvv.length < 3 -> { Toast.makeText(this, "CVV must be 3 digits", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            }

            // Simulate processing
            btnConfirm.isEnabled = false
            btnConfirm.text = "Processing..."

            // After short delay (simulated), process
            btnConfirm.postDelayed({
                processPayment(db, item.deposit, item.title)
            }, 1500)
        }
    }

    private fun processPayment(db: AppDatabaseHelper, amount: Double, title: String) {
        val userId = SessionManager.getUserId(this)
        val refNumber = "AF-${System.currentTimeMillis() % 1000000}"
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        db.reserveAccommodation(accomId, userId, refNumber, amount, date)

        // Go to receipt
        val intent = Intent(this, ReceiptActivity::class.java).apply {
            putExtra("REF_NUMBER", refNumber)
            putExtra("PROPERTY", title)
            putExtra("AMOUNT", amount)
            putExtra("DATE", date)
        }
        startActivity(intent)
        finish()
    }
}
