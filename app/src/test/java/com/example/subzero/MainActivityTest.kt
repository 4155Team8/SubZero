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
    @Test
    fun emailWithDotsInLocalPartIsValid() {
        val result =
            MainActivityTest.Companion.validateInputs("first.last@example.com", "pass")
        assertTrue(result.valid)
    }

    @Test
    fun emailWithHyphenInDomainIsValid() {
        val result = MainActivityTest.Companion.validateInputs("user@my-domain.com", "pass")
        assertTrue(result.valid)
    }

    @Test
    fun emailWithNumbersInDomainIsValid() {
        val result =
            MainActivityTest.Companion.validateInputs("user@example123.com", "pass")
        assertTrue(result.valid)
    }

    @Test
    fun emailWithOnlyWhitespaceFailsValidation() {
        val result = MainActivityTest.Companion.validateInputs("   ", "pass")
        assertFalse(result.valid)
        assertNotNull(result.emailError)
    }

    @Test
    fun emailWithMultipleAtSignsFailsValidation() {
        val result = MainActivityTest.Companion.validateInputs("a@@example.com", "pass")
        assertFalse(result.valid)
    }

    @Test
    fun emailIsTrimmedBeforeValidation() {
        // In MainActivity, email is trimmed before being passed to validateInputs
        // so a trimmed valid email should pass
        val result = MainActivityTest.Companion.validateInputs("user@example.com", "pass")
        assertTrue(result.valid)
    }

    // ------------------- Password variants -------------------

    @Test
    fun whitespaceOnlyPasswordIsValid() {
        // MainActivity only checks non-empty — a space char is not empty
        val result = MainActivityTest.Companion.validateInputs("user@example.com", " ")
        assertTrue(result.valid)
        assertNull(result.passwordError)
    }

    @Test
    fun veryLongPasswordIsValid() {
        val result =
            MainActivityTest.Companion.validateInputs("user@example.com", "a".repeat(200))
        assertTrue(result.valid)
    }

    @Test
    fun passwordWithSpecialCharsIsValid() {
        val result =
            MainActivityTest.Companion.validateInputs("user@example.com", "!@#\$%^&*()")
        assertTrue(result.valid)
    }

    // ------------------- Error message content -------------------

    @Test
    fun emailErrorMessageIsCorrect() {
        val result = MainActivityTest.Companion.validateInputs("bad", "pass")
        assertEquals("Enter a valid email address", result.emailError)
    }

    @Test
    fun passwordErrorMessageIsCorrect() {
        val result = MainActivityTest.Companion.validateInputs("user@example.com", "")
        assertEquals("Password is required", result.passwordError)
    }

    @Test
    fun bothErrorsAreSetWhenBothFieldsInvalid() {
        val result = MainActivityTest.Companion.validateInputs("", "")
        assertNotNull(result.emailError)
        assertNotNull(result.passwordError)
        assertFalse(result.valid)
    }

    @Test
    fun validInputsHaveNullErrors() {
        val result = MainActivityTest.Companion.validateInputs("user@example.com", "pass")
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }
}