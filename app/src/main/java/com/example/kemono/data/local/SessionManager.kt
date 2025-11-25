package com.example.kemono.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kemono_prefs", Context.MODE_PRIVATE)

    fun saveSessionCookie(cookie: String) {
        prefs.edit().putString("session_cookie", cookie).apply()
    }

    fun getSessionCookie(): String? {
        return prefs.getString("session_cookie", null)
    }

    fun clearSession() {
        prefs.edit().remove("session_cookie").apply()
    }

    fun hasSession(): Boolean {
        return !getSessionCookie().isNullOrEmpty()
    }
}
