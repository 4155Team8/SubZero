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
    @Test
    fun saveSessionWithEmptyTokenStillSavesIt() {
        SessionManager.saveSession(context, token = "", userId = 1, email = "a@b.com")
        // empty string is not null — isLoggedIn reads null check, so empty = "logged in"
        assertNotNull(SessionManager.getToken(context))
        assertEquals("", SessionManager.getToken(context))
    }

    @Test
    fun saveSessionWithLongTokenPreservesIt() {
        val longToken = "a".repeat(2048)
        SessionManager.saveSession(context, token = longToken, userId = 1, email = "a@b.com")
        assertEquals(longToken, SessionManager.getToken(context))
    }

    @Test
    fun saveSessionWithSpecialCharactersInTokenPreservesIt() {
        val specialToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.abc.xyz+/="
        SessionManager.saveSession(context, token = specialToken, userId = 1, email = "a@b.com")
        assertEquals(specialToken, SessionManager.getToken(context))
    }

    // ------------------- UserId edge cases -------------------

    @Test
    fun userIdOfZeroIsSavedAndRetrieved() {
        SessionManager.saveSession(context, token = "tok", userId = 0, email = "a@b.com")
        assertEquals(0, SessionManager.getUserId(context))
    }

    @Test
    fun userIdOfMaxIntIsSavedAndRetrieved() {
        SessionManager.saveSession(context, token = "tok", userId = Int.MAX_VALUE, email = "a@b.com")
        assertEquals(Int.MAX_VALUE, SessionManager.getUserId(context))
    }

    @Test
    fun negativeUserIdIsSavedAndRetrieved() {
        SessionManager.saveSession(context, token = "tok", userId = -99, email = "a@b.com")
        assertEquals(-99, SessionManager.getUserId(context))
    }

    // ------------------- Email edge cases -------------------

    @Test
    fun emailWithSpecialCharactersIsSavedAndRetrieved() {
        SessionManager.saveSession(context, token = "tok", userId = 1, email = "user+tag@mail.example.com")
        assertEquals("user+tag@mail.example.com", SessionManager.getUserEmail(context))
    }

    @Test
    fun emptyEmailIsSavedAndRetrieved() {
        SessionManager.saveSession(context, token = "tok", userId = 1, email = "")
        assertEquals("", SessionManager.getUserEmail(context))
    }

    // ------------------- clearSession is idempotent -------------------

    @Test
    fun clearSessionTwiceDoesNotThrow() {
        SessionManager.saveSession(context, token = "tok", userId = 1, email = "a@b.com")
        SessionManager.clearSession(context)
        SessionManager.clearSession(context) // second clear should be safe
        assertFalse(SessionManager.isLoggedIn(context))
    }

    @Test
    fun clearSessionOnEmptyStoreDoesNotThrow() {
        // already cleared in @Before — calling it again must not crash
        SessionManager.clearSession(context)
        assertNull(SessionManager.getToken(context))
    }

    // ------------------- Multiple overwrites -------------------

    @Test
    fun threeConsecutiveSavesKeepLastValues() {
        SessionManager.saveSession(context, token = "tok1", userId = 1, email = "a@b.com")
        SessionManager.saveSession(context, token = "tok2", userId = 2, email = "b@b.com")
        SessionManager.saveSession(context, token = "tok3", userId = 3, email = "c@b.com")
        assertEquals("tok3",    SessionManager.getToken(context))
        assertEquals("c@b.com", SessionManager.getUserEmail(context))
        assertEquals(3,         SessionManager.getUserId(context))
    }

    // ------------------- isLoggedIn after clear -------------------

    @Test
    fun isLoggedInReturnsFalseAfterClear() {
        SessionManager.saveSession(context, token = "tok", userId = 1, email = "a@b.com")
        assertTrue(SessionManager.isLoggedIn(context))
        SessionManager.clearSession(context)
        assertFalse(SessionManager.isLoggedIn(context))
    }
}