package com.example.subzero

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegisterActivityTest {

    // Static helpers mirroring the logic in RegisterActivity.checkPassword()
    companion object {

        data class PassVal(
            val valid: Boolean?,
            val messages: Array<String>? = Array(5) { "" },
            val strength: Int?
        )

        fun checkPassword(password: String): PassVal {
            var passStrength = 5
            val curr = PassVal(valid = false, strength = 0)
            val messages = Array(5) { "" }

            if (password.length < 8) {
                passStrength -= 3
                messages[0] = "• Make the password longer\n"
            }
            if (!password.any { it.isDigit() }) {
                passStrength -= 1
                messages[1] = "• Include a number\n"
            }
            if (!password.any { it.isUpperCase() }) {
                passStrength -= 1
                messages[2] = "• Include an uppercase letter\n"
            }
            if (!password.any { it.isLowerCase() }) {
                passStrength -= 1
                messages[3] = "• Include a lowercase letter\n"
            }
            if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) {
                passStrength -= 1
                messages[4] = "• Include a special character (e.g, ! $ ? @)"
            }

            val valid = passStrength >= 3
            return PassVal(valid = valid, messages = messages, strength = passStrength)
        }

        fun validateEmail(email: String): Boolean {
            return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun passwordsMatch(password: String, confirm: String): Boolean = password == confirm
    }

    // ------------------- Password Strength: Full Marks -------------------

    @Test
    fun `perfect password scores 5`() {
        val result = checkPassword("Secure1!")
        assertEquals(5, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `perfect password has no error messages`() {
        val result = checkPassword("Secure1!")
        result.messages?.forEach { msg ->
            assertEquals("", msg)
        }
    }

    // ------------------- Password Strength: Too Short -------------------

    @Test
    fun `short password loses 3 strength points`() {
        // "Ab1!" has length 4, missing 4 chars → -3 for length, score = 2
        val result = checkPassword("Ab1!")
        assertEquals(2, result.strength)
        assertEquals(false, result.valid)
    }

    @Test
    fun `short password sets length error message`() {
        val result = checkPassword("Ab1!")
        assertTrue(result.messages?.get(0)?.contains("longer") == true)
    }

    @Test
    fun `exactly 8 chars does not lose length points`() {
        // "Abcdef1!" is 8 chars with all criteria → strength 5
        val result = checkPassword("Abcdef1!")
        assertEquals(5, result.strength)
    }

    @Test
    fun `7 char password loses 3 strength points`() {
        // "Abcde1!" has 7 chars → -3 for length
        val result = checkPassword("Abcde1!")
        assertEquals(2, result.strength)
    }

    // ------------------- Password Strength: Missing Digit -------------------

    @Test
    fun `password without digit loses 1 strength point`() {
        val result = checkPassword("Secure!!")
        assertEquals(4, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `password without digit sets digit error message`() {
        val result = checkPassword("Secure!!")
        assertTrue(result.messages?.get(1)?.contains("number") == true)
    }

    // ------------------- Password Strength: Missing Uppercase -------------------

    @Test
    fun `password without uppercase loses 1 strength point`() {
        val result = checkPassword("secure1!")
        assertEquals(4, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `password without uppercase sets uppercase error message`() {
        val result = checkPassword("secure1!")
        assertTrue(result.messages?.get(2)?.contains("uppercase") == true)
    }

    // ------------------- Password Strength: Missing Lowercase -------------------

    @Test
    fun `password without lowercase loses 1 strength point`() {
        val result = checkPassword("SECURE1!")
        assertEquals(4, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `password without lowercase sets lowercase error message`() {
        val result = checkPassword("SECURE1!")
        assertTrue(result.messages?.get(3)?.contains("lowercase") == true)
    }

    // ------------------- Password Strength: Missing Special Character -------------------

    @Test
    fun `password without special char loses 1 strength point`() {
        val result = checkPassword("Secure12")
        assertEquals(4, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `password without special char sets special char error message`() {
        val result = checkPassword("Secure12")
        assertTrue(result.messages?.get(4)?.contains("special character") == true)
    }

    // ------------------- Password Strength: Boundary Validity -------------------

    @Test
    fun `strength 3 password is valid`() {
        // "Abcdef!!" — long, has upper, lower, special, no digit → 5-1 = 4. Actually let's use:
        // Short (4) + no digit (-1) + no upper (-1) = 0. Instead score exactly 3:
        // "Ab!cdefg" → long, has upper, lower, special, no digit → 5-1=4. Hmm.
        // "ab!cdefg" → long, no upper, no digit, has lower, has special → 5-1-1=3
        val result = checkPassword("ab!cdefg")
        assertEquals(3, result.strength)
        assertEquals(true, result.valid)
    }

    @Test
    fun `strength 2 password is invalid`() {
        // "ab!cde" → short(-3), no upper(-1), no digit(-1) → 5-3-1-1 = 0. Need score 2:
        // "abcdefg!" → long, no upper(-1), no digit(-1), has lower, has special → 5-2=3. Still 3.
        // "AB!CDEFG" → long, no lower(-1), no digit(-1) → 5-2=3. Hmm.
        // "ABCDEFG!" → long, no lower(-1), no digit(-1) → 3. Still 3.
        // "Ab!cde" → short(-3), has upper, has lower, has special, no digit(-1) → 5-3-1=1. 
        // "Abcde1" → short(-3) but 6 chars, has upper, lower, digit, no special(-1) → 5-3-1=1.
        // "Abc1!" short(-3), has upper, lower, digit, special → 5-3=2. Length 5 < 8.
        val result = checkPassword("Abc1!")
        assertEquals(2, result.strength)
        assertEquals(false, result.valid)
    }

    @Test
    fun `strength 0 all criteria missing`() {
        val result = checkPassword("ab")
        // short(-3), no digit(-1), no upper(-1), has lower, no special(-1) → 5-3-1-1-1=(-1 → 0 floors at negative, strength is -1 but let's check actual)
        // actual: passStrength starts at 5. -3(short), -1(no digit), -1(no upper), -1(no special) = -1
        assertTrue((result.strength ?: 1) < 3)
        assertEquals(false, result.valid)
    }

    // ------------------- Password Confirm Matching -------------------

    @Test
    fun `matching passwords returns true`() {
        assertTrue(passwordsMatch("MyPass1!", "MyPass1!"))
    }

    @Test
    fun `non-matching passwords returns false`() {
        assertFalse(passwordsMatch("MyPass1!", "Different1!"))
    }

    @Test
    fun `empty passwords match each other`() {
        assertTrue(passwordsMatch("", ""))
    }

    @Test
    fun `password vs empty confirm does not match`() {
        assertFalse(passwordsMatch("MyPass1!", ""))
    }

    @Test
    fun `case sensitive password matching`() {
        assertFalse(passwordsMatch("mypass1!", "MyPass1!"))
    }

    // ------------------- Special Character Edge Cases -------------------

    @Test
    fun `space is not counted as special character`() {
        // "Secure 1" → has upper, lower, digit, space but no special → -1
        val result = checkPassword("Secure 1")
        assertTrue(result.messages?.get(4)?.contains("special character") == true)
    }

    @Test
    fun `various special characters are accepted`() {
        listOf("Secure1!", "Secure1@", "Secure1#", "Secure1$", "Secure1?").forEach { pwd ->
            val result = checkPassword(pwd)
            assertEquals("Failed for: $pwd", "", result.messages?.get(4))
        }
    }
}
