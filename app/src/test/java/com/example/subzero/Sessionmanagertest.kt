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
    fun `isLoggedIn returns false when no session saved`() {
        assertFalse(SessionManager.isLoggedIn(context))
    }

    @Test
    fun `isLoggedIn returns true after saving session`() {
        SessionManager.saveSession(context, token = "test_token", userId = 1, email = "test@example.com")
        assertTrue(SessionManager.isLoggedIn(context))
    }

    @Test
    fun `getToken returns null when no session saved`() {
        assertNull(SessionManager.getToken(context))
    }

    @Test
    fun `getToken returns saved token`() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        assertEquals("abc123", SessionManager.getToken(context))
    }

    @Test
    fun `getUserEmail returns saved email`() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        assertEquals("test@example.com", SessionManager.getUserEmail(context))
    }

    @Test
    fun `getUserId returns saved user id`() {
        SessionManager.saveSession(context, token = "abc123", userId = 42, email = "test@example.com")
        assertEquals(42, SessionManager.getUserId(context))
    }

    @Test
    fun `getUserId returns -1 when no session saved`() {
        assertEquals(-1, SessionManager.getUserId(context))
    }

    @Test
    fun `clearSession removes all saved data`() {
        SessionManager.saveSession(context, token = "abc123", userId = 1, email = "test@example.com")
        SessionManager.clearSession(context)

        assertNull(SessionManager.getToken(context))
        assertNull(SessionManager.getUserEmail(context))
        assertEquals(-1, SessionManager.getUserId(context))
        assertFalse(SessionManager.isLoggedIn(context))
    }

    @Test
    fun `saveSession overwrites previous session`() {
        SessionManager.saveSession(context, token = "old_token", userId = 1, email = "old@example.com")
        SessionManager.saveSession(context, token = "new_token", userId = 2, email = "new@example.com")

        assertEquals("new_token", SessionManager.getToken(context))
        assertEquals("new@example.com", SessionManager.getUserEmail(context))
        assertEquals(2, SessionManager.getUserId(context))
    }
}