package com.accomfind.app

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accomfind.app.data.AppDatabaseHelper
import com.accomfind.app.data.ChatMessage
import com.google.android.material.button.MaterialButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ChatActivity : AppCompatActivity() {

    private lateinit var db: AppDatabaseHelper
    private var accomId: Int = -1
    private var userId: Int = -1
    private lateinit var rvChat: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var etMessage: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        db = AppDatabaseHelper(this)
        accomId = intent.getIntExtra("ACCOM_ID", -1)
        userId = SessionManager.getUserId(this)

        if (accomId == -1) { finish(); return }

        val accom = db.getAccommodationById(accomId) ?: run { finish(); return }
        
        findViewById<TextView>(R.id.tvChatTitle).text = "Chat: ${accom.title}"
        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)
        val btnAgreement = findViewById<MaterialButton>(R.id.btnAgreement)

        chatAdapter = ChatAdapter(userId)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        loadChatHistory()

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                db.sendMessage(userId, accomId, "Student", content)
                etMessage.setText("")
                loadChatHistory()
                
                // Simulate landlord response
                rvChat.postDelayed({
                    simulateLandlordResponse(content)
                }, 1000)
            }
        }

        // Agreement Logic
        val status = db.getAgreementStatus(userId, accomId)
        if (status == "Confirmed") {
            btnAgreement.text = "Agreement Confirmed"
            btnAgreement.isEnabled = false
            btnAgreement.alpha = 0.7f
        } else {
            btnAgreement.setOnClickListener {
                showAgreementDialog()
            }
        }
    }

    private fun loadChatHistory() {
        val history = db.getChatHistory(userId, accomId)
        chatAdapter.setMessages(history)
        if (history.isNotEmpty()) {
            rvChat.scrollToPosition(history.size - 1)
        }
    }

    private fun simulateLandlordResponse(studentMsg: String) {
        val response = when {
            studentMsg.contains("hello", true) || studentMsg.contains("hi", true) -> "Hello! How can I help you with this property?"
            studentMsg.contains("view", true) -> "Sure, when would you like to come and see the place?"
            studentMsg.contains("price", true) || studentMsg.contains("rent", true) -> "The rent is as listed, and it includes water and security."
            studentMsg.contains("agreement", true) || studentMsg.contains("contract", true) -> "I can send over the rental agreement for you to confirm."
            else -> "Thank you for your message. I will get back to you shortly."
        }
        db.sendMessage(userId, accomId, "Landlord", response)
        loadChatHistory()
    }

    private fun showAgreementDialog() {
        AlertDialog.Builder(this)
            .setTitle("Rental Agreement")
            .setMessage("Do you agree to the terms and conditions of renting this property? By clicking 'I Agree', you commit to the rental and the property will be marked as Rented.")
            .setPositiveButton("I Agree") { _, _ ->
                db.confirmAgreement(userId, accomId)
                Toast.makeText(this, "Agreement Confirmed! Property is now yours.", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class ChatAdapter(private val currentUserId: Int) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
        private var messages = listOf<ChatMessage>()

        fun setMessages(newMessages: List<ChatMessage>) {
            messages = newMessages
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val msg = messages[position]
            val isStudent = msg.sender == "Student"
            
            holder.llStudent.isVisible = isStudent
            holder.llLandlord.isVisible = !isStudent
            
            if (isStudent) {
                holder.tvStudentMsg.text = msg.content
            } else {
                holder.tvLandlordMsg.text = msg.content
            }
        }

        override fun getItemCount() = messages.size

        class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val llStudent: LinearLayout = view.findViewById(R.id.llStudentMessage)
            val llLandlord: LinearLayout = view.findViewById(R.id.llLandlordMessage)
            val tvStudentMsg: TextView = view.findViewById(R.id.tvStudentMessage)
            val tvLandlordMsg: TextView = view.findViewById(R.id.tvLandlordMessage)
        }
    }
}
