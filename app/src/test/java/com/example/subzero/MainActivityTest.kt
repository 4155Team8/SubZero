package com.example.subzero

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {

    // Mirror of MainActivity.validateInputs logic, extracted for unit testing
    companion object {
        data class ValidationResult(
            val valid: Boolean,
            val emailError: String?,
            val passwordError: String?
        )

        fun validateInputs(email: String, password: String): ValidationResult {
            var valid = true
            var emailError: String? = null
            var passwordError: String? = null

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailError = "Enter a valid email address"
                valid = false
            }

            if (password.isEmpty()) {
                passwordError = "Password is required"
                valid = false
            }

            return ValidationResult(valid, emailError, passwordError)
        }
    }

    // ------------------- Valid inputs -------------------

    @Test
    fun validEmailAndPasswordReturnsTrue() {
        val result = validateInputs("user@example.com", "password123")
        assertTrue(result.valid)
    }

    @Test
    fun validInputsProduceNoErrors() {
        val result = validateInputs("user@example.com", "password123")
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }

    // ------------------- Email validation -------------------

    @Test
    fun emptyEmailReturnsInvalid() {
        val result = validateInputs("", "password123")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
    }

    @Test
    fun emailWithoutAtSignReturnsInvalid() {
        val result = validateInputs("notanemail", "password123")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
    }

    @Test
    fun emailWithoutDomainReturnsInvalid() {
        val result = validateInputs("user@", "password123")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
    }

    @Test
    fun emailWithoutUsernameReturnsInvalid() {
        val result = validateInputs("@example.com", "password123")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
    }

    @Test
    fun emailWithSubdomainIsValid() {
        val result = validateInputs("user@mail.example.com", "password123")
        assertTrue(result.valid)
    }

    @Test
    fun emailWithPlusAddressingIsValid() {
        val result = validateInputs("user+tag@example.com", "password123")
        assertTrue(result.valid)
    }

    // ------------------- Password validation -------------------

    @Test
    fun emptyPasswordReturnsInvalid() {
        val result = validateInputs("user@example.com", "")
        assertFalse(result.valid)
        assertNotNull(result.passwordError)
    }

    @Test
    fun singleCharacterPasswordIsValid() {
        // MainActivity only checks non-empty, not strength
        val result = validateInputs("user@example.com", "x")
        assertTrue(result.valid)
        assertNull(result.passwordError)
    }

    // ------------------- Both fields invalid -------------------

    @Test
    fun emptyEmailAndEmptyPasswordBothFail() {
        val result = validateInputs("", "")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
        assertNotNull(result.passwordError)
    }

    @Test
    fun invalidEmailAndEmptyPasswordBothReportErrors() {
        val result = validateInputs("bademail", "")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
        assertNotNull(result.passwordError)
    }
}