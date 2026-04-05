package com.example.subzero

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        SessionManager.clearSession(context)
    }

    @Test
    fun isLoggedInReturnsFalseWhenNoSessionSaved() {
        assertFalse(SessionManager.isLoggedIn(context))
    }

    @Test
    fun isLoggedInReturnsTrueAfterSavingSession() {
        SessionManager.saveSession(context, token = "test_token", userId = 1, email = "test@example.com")
        assertTrue(SessionManager.isLoggedIn(context))
    }

    @Test
    fun getTokenReturnsNullWhenNoSessionSaved() {
        assertNull(SessionManager.getToken(context))
    }

    @Test
    fun getTokenReturnsSavedToken() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        assertEquals("abc123", SessionManager.getToken(context))
    }

    @Test
    fun getUserEmailReturnsSavedEmail() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        assertEquals("test@example.com", SessionManager.getUserEmail(context))
    }

    @Test
    fun getUserIdReturnsSavedUserID() {
        SessionManager.saveSession(context, token = "abc123", userId = 42, email = "test@example.com")
        assertEquals(42, SessionManager.getUserId(context))
    }

    @Test
    fun getUserIDreturnsNeg1WhenNoSessionSaved() {
        assertEquals(-1, SessionManager.getUserId(context))
    }

    @Test
    fun clearSessionRemovesAllSavedData() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        SessionManager.clearSession(context)

        assertNull(SessionManager.getToken(context))
        assertNull(SessionManager.getUserEmail(context))
        assertEquals(-1, SessionManager.getUserId(context))
        assertFalse(SessionManager.isLoggedIn(context))
    }

    @Test
    fun saveSessionOverwritesPreviousSession() {
        SessionManager.saveSession(context, token = "old_token", userId = 1, email = "old@example.com")
        SessionManager.saveSession(context, token = "new_token", userId = 2, email = "new@example.com")

        assertEquals("new_token", SessionManager.getToken(context))
        assertEquals("new@example.com", SessionManager.getUserEmail(context))
        assertEquals(2, SessionManager.getUserId(context))
    }
}