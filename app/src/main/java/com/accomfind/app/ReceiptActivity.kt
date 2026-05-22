package com.accomfind.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val refNumber = intent.getStringExtra("REF_NUMBER") ?: "N/A"
        val property = intent.getStringExtra("PROPERTY") ?: "N/A"
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        val date = intent.getStringExtra("DATE") ?: "N/A"

        findViewById<TextView>(R.id.tvReceiptRef).text = refNumber
        findViewById<TextView>(R.id.tvReceiptProperty).text = property
        findViewById<TextView>(R.id.tvReceiptAmount).text = "BWP ${String.format("%,.0f", amount)}"
        findViewById<TextView>(R.id.tvReceiptDate).text = date

        findViewById<MaterialButton>(R.id.btnDone).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
