// ============================================================
// FILE 1: ForgotPasswordActivityTest.kt
// ============================================================
package com.example.subzero


import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ForgotPasswordActivityTest {

    companion object {
        fun validateEmail(email: String): Pair<Boolean, String?> {
            return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Pair(false, "Enter a valid email address")
            } else {
                Pair(true, null)
            }
        }
    }

    // ------------------- Valid email -------------------

    @Test
    fun validEmailPassesValidation() {
        val (valid, error) = validateEmail("user@example.com")
        assertTrue(valid)
        assertNull(error)
    }

    @Test
    fun validEmailWithSubdomainPassesValidation() {
        val (valid, _) = validateEmail("user@mail.example.com")
        assertTrue(valid)
    }

    @Test
    fun validEmailWithPlusAddressingPassesValidation() {
        val (valid, _) = validateEmail("user+tag@example.com")
        assertTrue(valid)
    }

    @Test
    fun validEmailWithNumbersPassesValidation() {
        val (valid, _) = validateEmail("user123@example.com")
        assertTrue(valid)
    }

    // ------------------- Invalid email -------------------

    @Test
    fun emptyEmailFailsValidation() {
        val (valid, error) = validateEmail("")
        assertFalse(valid)
        assertEquals("Enter a valid email address", error)
    }

    @Test
    fun emailWithoutAtSignFailsValidation() {
        val (valid, error) = validateEmail("notanemail")
        assertFalse(valid)
        assertNotNull(error)
    }

    @Test
    fun emailWithoutDomainFailsValidation() {
        val (valid, _) = validateEmail("user@")
        assertFalse(valid)
    }

    @Test
    fun emailWithoutUsernameFailsValidation() {
        val (valid, _) = validateEmail("@example.com")
        assertFalse(valid)
    }

    @Test
    fun emailWithSpacesFailsValidation() {
        val (valid, _) = validateEmail("user @example.com")
        assertFalse(valid)
    }

    @Test
    fun emailMissingTldFailsValidation() {
        val (valid, _) = validateEmail("user@example")
        assertFalse(valid)
    }

    @Test
    fun plainTextEmailFailsValidation() {
        val (valid, _) = validateEmail("hello world")
        assertFalse(valid)
    }

    // ------------------- Error message content -------------------

    @Test
    fun invalidEmailProducesCorrectErrorMessage() {
        val (_, error) = validateEmail("bademail")
        assertEquals("Enter a valid email address", error)
    }

    @Test
    fun emptyStringProducesErrorMessage() {
        val (_, error) = validateEmail("")
        assertNotNull(error)
    }
}
