package com.accomfind.app

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "accomfind_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_FILTER_MAX_PRICE = "filter_max_price"
    private const val KEY_FILTER_LOCATION = "filter_location"
    private const val KEY_FILTER_DATE = "filter_date"
    private const val KEY_SAVE_PREFS = "save_prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun login(context: Context, userId: Int, username: String, fullName: String, studentId: String) {
        prefs(context).edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_STUDENT_ID, studentId)
            apply()
        }
    }

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1

    fun getUserId(context: Context): Int = prefs(context).getInt(KEY_USER_ID, -1)

    fun getUsername(context: Context): String = prefs(context).getString(KEY_USERNAME, "") ?: ""

    fun getFullName(context: Context): String = prefs(context).getString(KEY_FULL_NAME, "") ?: ""

    fun getStudentId(context: Context): String = prefs(context).getString(KEY_STUDENT_ID, "") ?: ""

    // Filter preferences
    fun saveFilterPrefs(context: Context, maxPrice: Double, location: String?, date: String?) {
        prefs(context).edit().apply {
            putFloat(KEY_FILTER_MAX_PRICE, maxPrice.toFloat())
            putString(KEY_FILTER_LOCATION, location ?: "")
            putString(KEY_FILTER_DATE, date ?: "")
            putBoolean(KEY_SAVE_PREFS, true)
            apply()
        }
    }

    fun getFilterMaxPrice(context: Context): Double =
        prefs(context).getFloat(KEY_FILTER_MAX_PRICE, 5000f).toDouble()

    fun getFilterLocation(context: Context): String? {
        val loc = prefs(context).getString(KEY_FILTER_LOCATION, "") ?: ""
        return if (loc.isEmpty()) null else loc
    }

    fun getFilterDate(context: Context): String? {
        val d = prefs(context).getString(KEY_FILTER_DATE, "") ?: ""
        return if (d.isEmpty()) null else d
    }

    fun hasSavedPrefs(context: Context): Boolean = prefs(context).getBoolean(KEY_SAVE_PREFS, false)
}
