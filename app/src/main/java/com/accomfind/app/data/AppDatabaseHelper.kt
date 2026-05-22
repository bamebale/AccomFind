package com.accomfind.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "accomfind_v2.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT,
                student_id TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE accommodations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                location TEXT NOT NULL,
                type TEXT NOT NULL,
                amenities TEXT,
                availability_date TEXT,
                deposit REAL,
                image_res_name TEXT,
                status TEXT DEFAULT 'Available',
                distance TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE reservations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                accom_id INTEGER,
                user_id INTEGER,
                reference_number TEXT,
                amount_paid REAL,
                reserved_date TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE saved (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                accom_id INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                accom_id INTEGER,
                sender TEXT,
                content TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE agreements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                accom_id INTEGER,
                status TEXT DEFAULT 'Pending',
                confirmed_date TEXT
            )
        """.trimIndent())

        // Seed all data in one transaction for performance
        db.beginTransaction()
        try {
            seedUsers(db)
            seedAccommodations(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS accommodations")
        db.execSQL("DROP TABLE IF EXISTS reservations")
        db.execSQL("DROP TABLE IF EXISTS saved")
        db.execSQL("DROP TABLE IF EXISTS messages")
        db.execSQL("DROP TABLE IF EXISTS agreements")
        onCreate(db)
    }

    private fun seedUsers(db: SQLiteDatabase) {
        val firstNames = listOf("Kagiso","Tebogo","Lerato","Boitumelo","Tshepo","Kelebogile","Moagi",
            "Dineo","Thabo","Kefilwe","Sebolaelo","Mpho","Kgomotso","Phenyo","Oarabile",
            "Gaone","Kabo","Tlotlo","Lorato","Refilwe","Neo","Oteng","Keone","Kemi","Dumi",
            "Baraka","Lebogang","Gosego","Bame","Ookeditse","Lesego","Modiri","Thumelo",
            "Gape","Kenaleone","Kelang","Onkemetse","Thapelo","Naledi","Malebogo",
            "Kabelo","Gontse","Sethunya","Masego","Kenosi","Segaetsho","Pako","Keoagile",
            "Bathusi","Loago")
        firstNames.forEachIndexed { i, name ->
            val cv = ContentValues().apply {
                put("username", "student${i + 1}")
                put("password", "pass${i + 1}")
                put("full_name", name)
                put("student_id", "CSE24-${String.format("%03d", i + 1)}")
            }
            db.insert("users", null, cv)
        }
    }

    private fun seedAccommodations(db: SQLiteDatabase) {
        val data = listOf(
            Triple("Modern Student Villa", 2500.0, "Gaborone West"),
            Triple("Cozy Block 8 Studio", 1800.0, "Block 8"),
            Triple("Tlokweng Garden Flat", 3200.0, "Tlokweng"),
            Triple("Luxury Phakalane Suite", 4500.0, "Phakalane"),
            Triple("Broadhurst Student Room", 1500.0, "Broadhurst")
        )

        val amenitiesList = listOf(
            "WiFi, Parking, Security, Water",
            "WiFi, Water, Electric Fence",
            "Parking, Security, DSTV",
            "WiFi, Security, Water, Garage",
            "WiFi, Parking, Security, Swimming Pool"
        )
        
        val distances = listOf("0.8km from UB", "1.2km from UB", "2.1km from UB", "3.5km from UB", "1.8km from UB")
        val months = listOf("2024-05-01", "2024-06-01", "2024-07-01", "2024-08-01", "2024-05-01")

        data.forEachIndexed { i, triple ->
            val cv = ContentValues().apply {
                put("title", triple.first)
                put("price", triple.second)
                put("location", triple.third)
                put("type", if (i % 2 == 0) "House" else "Apartment")
                put("amenities", amenitiesList[i])
                put("availability_date", months[i])
                put("deposit", triple.second * 1.5)
                put("image_res_name", "house_img_${i + 1}")
                put("status", "Available")
                put("distance", distances[i])
            }
            db.insert("accommodations", null, cv)
        }
    }

    // ---- User operations ----

    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        return if (cursor.moveToFirst()) {
            User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")) ?: "",
                studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")) ?: ""
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }

    fun registerUser(username: String, password: String, fullName: String, studentId: String): Boolean {
        val db = writableDatabase
        return try {
            val cv = ContentValues().apply {
                put("username", username)
                put("password", password)
                put("full_name", fullName)
                put("student_id", studentId)
            }
            db.insertOrThrow("users", null, cv)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---- Accommodation operations ----

    fun getAllAccommodations(includeReserved: Boolean = false, includeRented: Boolean = false): List<Accommodation> {
        val list = mutableListOf<Accommodation>()
        val db = readableDatabase
        val query = when {
            includeReserved && includeRented -> "SELECT * FROM accommodations ORDER BY id ASC"
            includeReserved -> "SELECT * FROM accommodations WHERE status != 'Rented' ORDER BY id ASC"
            includeRented -> "SELECT * FROM accommodations WHERE status != 'Reserved' ORDER BY id ASC"
            else -> "SELECT * FROM accommodations WHERE status = 'Available' ORDER BY id ASC"
        }
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            list.add(cursorToAccom(cursor))
        }
        cursor.close()
        return list
    }

    fun getFilteredAccommodations(
        maxPrice: Double = 5000.0,
        location: String? = null,
        date: String? = null,
        includeReserved: Boolean = false,
        includeRented: Boolean = false
    ): List<Accommodation> {
        val list = mutableListOf<Accommodation>()
        val db = readableDatabase

        var query = "SELECT * FROM accommodations WHERE price <= ?"
        val args = mutableListOf<String>(maxPrice.toString())

        if (!includeReserved && !includeRented) {
            query += " AND status = 'Available'"
        } else if (!includeReserved) {
            query += " AND status != 'Reserved'"
        } else if (!includeRented) {
            query += " AND status != 'Rented'"
        }

        if (!location.isNullOrEmpty()) {
            query += " AND location = ?"
            args.add(location)
        }
        if (!date.isNullOrEmpty()) {
            query += " AND availability_date <= ?"
            args.add(date)
        }
        query += " ORDER BY price ASC"

        val cursor = db.rawQuery(query, args.toTypedArray())
        while (cursor.moveToNext()) {
            list.add(cursorToAccom(cursor))
        }
        cursor.close()
        return list
    }

    fun searchAccommodations(query: String): List<Accommodation> {
        val list = mutableListOf<Accommodation>()
        val db = readableDatabase
        val pattern = "%$query%"
        val cursor = db.rawQuery(
            "SELECT * FROM accommodations WHERE title LIKE ? OR location LIKE ? OR type LIKE ?",
            arrayOf(pattern, pattern, pattern)
        )
        while (cursor.moveToNext()) {
            list.add(cursorToAccom(cursor))
        }
        cursor.close()
        return list
    }

    fun getAccommodationById(id: Int): Accommodation? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM accommodations WHERE id = ?", arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            cursorToAccom(cursor).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }

    fun reserveAccommodation(accomId: Int, userId: Int, referenceNumber: String, amountPaid: Double, date: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Update status to Reserved
            val cv = ContentValues().apply { put("status", "Reserved") }
            db.update("accommodations", cv, "id = ?", arrayOf(accomId.toString()))

            // Insert reservation record
            val rv = ContentValues().apply {
                put("accom_id", accomId)
                put("user_id", userId)
                put("reference_number", referenceNumber)
                put("amount_paid", amountPaid)
                put("reserved_date", date)
            }
            db.insert("reservations", null, rv)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getUserReservations(userId: Int): List<Accommodation> {
        val list = mutableListOf<Accommodation>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT a.* FROM accommodations a
               INNER JOIN reservations r ON a.id = r.accom_id
               WHERE r.user_id = ?""",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(cursorToAccom(cursor))
        }
        cursor.close()
        return list
    }

    // ---- Saved operations ----

    fun saveAccommodation(userId: Int, accomId: Int) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("accom_id", accomId)
        }
        db.insertWithOnConflict("saved", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun unsaveAccommodation(userId: Int, accomId: Int) {
        val db = writableDatabase
        db.delete("saved", "user_id = ? AND accom_id = ?", arrayOf(userId.toString(), accomId.toString()))
    }

    fun isAccommodationSaved(userId: Int, accomId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM saved WHERE user_id = ? AND accom_id = ?",
            arrayOf(userId.toString(), accomId.toString())
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    fun getSavedAccommodations(userId: Int): List<Accommodation> {
        val list = mutableListOf<Accommodation>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT a.* FROM accommodations a
               INNER JOIN saved s ON a.id = s.accom_id
               WHERE s.user_id = ?""",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(cursorToAccom(cursor))
        }
        cursor.close()
        return list
    }

    // ---- Chat & Agreement operations ----
    fun sendMessage(userId: Int, accomId: Int, sender: String, content: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("accom_id", accomId)
            put("sender", sender)
            put("content", content)
        }
        db.insert("messages", null, cv)
    }

    fun getChatHistory(userId: Int, accomId: Int): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM messages WHERE user_id = ? AND accom_id = ? ORDER BY timestamp ASC",
            arrayOf(userId.toString(), accomId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(ChatMessage(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                accomId = cursor.getInt(cursor.getColumnIndexOrThrow("accom_id")),
                sender = cursor.getString(cursor.getColumnIndexOrThrow("sender")),
                content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
            ))
        }
        cursor.close()
        return list
    }

    fun createAgreement(userId: Int, accomId: Int) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("accom_id", accomId)
            put("status", "Pending")
        }
        db.insert("agreements", null, cv)
    }

    fun confirmAgreement(userId: Int, accomId: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Update agreement
            val cv = ContentValues().apply {
                put("status", "Confirmed")
                put("confirmed_date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            }
            db.update("agreements", cv, "user_id = ? AND accom_id = ?", arrayOf(userId.toString(), accomId.toString()))

            // Mark accommodation as Rented
            val av = ContentValues().apply { put("status", "Rented") }
            db.update("accommodations", av, "id = ?", arrayOf(accomId.toString()))

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAgreementStatus(userId: Int, accomId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT status FROM agreements WHERE user_id = ? AND accom_id = ?",
            arrayOf(userId.toString(), accomId.toString())
        )
        return if (cursor.moveToFirst()) {
            cursor.getString(0).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }

    // ---- Helper ----

    private fun cursorToAccom(cursor: android.database.Cursor): Accommodation {
        return Accommodation(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
            location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
            amenities = cursor.getString(cursor.getColumnIndexOrThrow("amenities")) ?: "",
            availabilityDate = cursor.getString(cursor.getColumnIndexOrThrow("availability_date")) ?: "",
            deposit = cursor.getDouble(cursor.getColumnIndexOrThrow("deposit")),
            imageResName = cursor.getString(cursor.getColumnIndexOrThrow("image_res_name")) ?: "house_img_1",
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "Available",
            distance = cursor.getString(cursor.getColumnIndexOrThrow("distance")) ?: ""
        )
    }
}
