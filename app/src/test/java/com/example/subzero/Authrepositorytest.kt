package com.example.subzero.network

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.reset
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthRepositoryTest {

    @Mock
    private lateinit var mockApiService: ApiService

    // set up the mock api service
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        ApiClient.setInstanceForTesting(mockApiService)
    }

    // clear after done
    @After
    fun tearDown() {
        ApiClient.clearTestInstance()
    }

    // login tests vvvv

    @Test
    fun loginReturnsSuccessWithTokenOnValidCredentials() = runBlocking {
        `when`(mockApiService.login(any())).thenReturn(
            Response.success(
                LoginResponse("Login successful", "mock_jwt_token", AuthUser(1, "test@example.com"))
            )
        )

        val result = AuthRepository.login("test@example.com", "password123")

        assertTrue("Expected Success but got: $result", result is AuthResult.Success)
        val data = (result as AuthResult.Success).data
        assertEquals("mock_jwt_token", data.token)
        assertEquals("test@example.com", data.user.email)
    }

    @Test
    fun loginReturnsErrorOnInvalidCredentials() = runBlocking {
        val errorBody = """{"error":"Invalid email or password"}"""
            .toResponseBody("application/json".toMediaType())

        `when`(mockApiService.login(any())).thenReturn(Response.error(401, errorBody))

        val result = AuthRepository.login("test@example.com", "wrongpassword")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertEquals("Invalid email or password", (result as AuthResult.Error).message)
    }

    @Test
    fun loginReturnsErrorOnNetworkException() = runBlocking {
        `when`(mockApiService.login(any())).thenThrow(RuntimeException("Network unavailable"))

        val result = AuthRepository.login("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }

    // registration tests vvvvv

    @Test
    fun registerReturnsSuccessOnValidData() = runBlocking {
        `when`(mockApiService.register(any())).thenReturn(
            Response.success(
                RegisterResponse("User registered successfully", AuthUser(2, "new@example.com"))
            )
        )

        val result = AuthRepository.register("new@example.com", "password123")

        assertTrue("Expected Success but got: $result", result is AuthResult.Success)
        assertEquals("new@example.com", (result as AuthResult.Success).data.user.email)
    }

    @Test
    fun registerReturnsErrorOnDuplicateEmail() = runBlocking {
        val errorBody = """{"error":"Email is already registered"}"""
            .toResponseBody("application/json".toMediaType())

        `when`(mockApiService.register(any())).thenReturn(Response.error(409, errorBody))

        val result = AuthRepository.register("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertEquals("Email is already registered", (result as AuthResult.Error).message)
    }

    @Test
    fun registerReturnsErrorOnNetworkException() = runBlocking {
        `when`(mockApiService.register(any())).thenThrow(RuntimeException("Timeout"))

        val result = AuthRepository.register("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }
    // ------------------- Login: token content -------------------

    @Test
    fun loginSuccessPreservesUserIdFromResponse() = runBlocking {
        `when`(mockApiService.login(any())).thenReturn(
            Response.success(LoginResponse("OK", "tok", AuthUser(99, "a@b.com")))
        )
        val result = AuthRepository.login("a@b.com", "pass")
        assertEquals(99, (result as AuthResult.Success).data.user.id)
    }

    @Test
    fun loginSuccessPreservesMessageFromResponse() = runBlocking {
        `when`(mockApiService.login(any())).thenReturn(
            Response.success(LoginResponse("Login successful", "tok", AuthUser(1, "a@b.com")))
        )
        val result = AuthRepository.login("a@b.com", "pass")
        assertEquals("Login successful", (result as AuthResult.Success).data.message)
    }

    @Test
    fun loginSuccessTokenIsNotEmpty() = runBlocking {
        `when`(mockApiService.login(any())).thenReturn(
            Response.success(LoginResponse("OK", "real_token_here", AuthUser(1, "a@b.com")))
        )
        val result = AuthRepository.login("a@b.com", "pass")
        assertTrue((result as AuthResult.Success).data.token.isNotEmpty())
    }

    // ------------------- Login: HTTP error codes -------------------

    @Test
    fun loginReturnsErrorOn403Forbidden() = runBlocking {
        val errorBody = """{"error":"Forbidden"}""".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.login(any())).thenReturn(Response.error(403, errorBody))
        val result = AuthRepository.login("a@b.com", "pass")
        assertTrue(result is AuthResult.Error)
        assertEquals("Forbidden", (result as AuthResult.Error).message)
    }

    @Test
    fun loginReturnsErrorOn500ServerError() = runBlocking {
        val errorBody = """{"error":"Internal server error"}""".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.login(any())).thenReturn(Response.error(500, errorBody))
        val result = AuthRepository.login("a@b.com", "pass")
        assertTrue(result is AuthResult.Error)
    }

    @Test
    fun loginErrorMessageFallsBackWhenBodyIsNull() = runBlocking {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.login(any())).thenReturn(Response.error(401, errorBody))
        val result = AuthRepository.login("a@b.com", "pass")
        assertTrue(result is AuthResult.Error)
        // fallback message when error field is missing
        assertEquals("Login failed", (result as AuthResult.Error).message)
    }

    @Test
    fun loginNetworkErrorMessageContainsNetworkError() = runBlocking {
        `when`(mockApiService.login(any())).thenThrow(RuntimeException("Timeout"))
        val result = AuthRepository.login("a@b.com", "pass")
        assertTrue((result as AuthResult.Error).message.startsWith("Network error"))
    }

    // ------------------- Register: HTTP error codes -------------------

    @Test
    fun registerSuccessPreservesUserEmail() = runBlocking {
        `when`(mockApiService.register(any())).thenReturn(
            Response.success(RegisterResponse("OK", AuthUser(5, "new@example.com")))
        )
        val result = AuthRepository.register("new@example.com", "Pass1!")
        assertEquals("new@example.com", (result as AuthResult.Success).data.user.email)
    }

    @Test
    fun registerSuccessPreservesUserId() = runBlocking {
        `when`(mockApiService.register(any())).thenReturn(
            Response.success(RegisterResponse("OK", AuthUser(5, "new@example.com")))
        )
        val result = AuthRepository.register("new@example.com", "Pass1!")
        assertEquals(5, (result as AuthResult.Success).data.user.id)
    }

    @Test
    fun registerReturnsErrorOn400BadRequest() = runBlocking {
        val errorBody = """{"error":"Invalid input"}""".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.register(any())).thenReturn(Response.error(400, errorBody))
        val result = AuthRepository.register("bad", "pass")
        assertTrue(result is AuthResult.Error)
        assertEquals("Invalid input", (result as AuthResult.Error).message)
    }

    @Test
    fun registerReturnsErrorOn500() = runBlocking {
        val errorBody = """{"error":"Server error"}""".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.register(any())).thenReturn(Response.error(500, errorBody))
        val result = AuthRepository.register("a@b.com", "Pass1!")
        assertTrue(result is AuthResult.Error)
    }

    @Test
    fun registerNetworkErrorMessageContainsNetworkError() = runBlocking {
        `when`(mockApiService.register(any())).thenThrow(RuntimeException("No route to host"))
        val result = AuthRepository.register("a@b.com", "Pass1!")
        assertTrue((result as AuthResult.Error).message.startsWith("Network error"))
    }

    @Test
    fun registerErrorFallbackMessageWhenBodyMissingField() = runBlocking {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        `when`(mockApiService.register(any())).thenReturn(Response.error(409, errorBody))
        val result = AuthRepository.register("a@b.com", "Pass1!")
        assertTrue(result is AuthResult.Error)
        assertEquals("Registration failed", (result as AuthResult.Error).message)
    }
}