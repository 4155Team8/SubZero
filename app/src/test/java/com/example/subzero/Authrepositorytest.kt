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
    fun `login returns Success with token on valid credentials`() = runBlocking {
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
    fun `login returns Error on invalid credentials`() = runBlocking {
        val errorBody = """{"error":"Invalid email or password"}"""
            .toResponseBody("application/json".toMediaType())

        `when`(mockApiService.login(any())).thenReturn(Response.error(401, errorBody))

        val result = AuthRepository.login("test@example.com", "wrongpassword")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertEquals("Invalid email or password", (result as AuthResult.Error).message)
    }

    @Test
    fun `login returns Error on network exception`() = runBlocking {
        `when`(mockApiService.login(any())).thenThrow(RuntimeException("Network unavailable"))

        val result = AuthRepository.login("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }

    // registration tests vvvvv

    @Test
    fun `register returns Success on valid data`() = runBlocking {
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
    fun `register returns Error on duplicate email`() = runBlocking {
        val errorBody = """{"error":"Email is already registered"}"""
            .toResponseBody("application/json".toMediaType())

        `when`(mockApiService.register(any())).thenReturn(Response.error(409, errorBody))

        val result = AuthRepository.register("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertEquals("Email is already registered", (result as AuthResult.Error).message)
    }

    @Test
    fun `register returns Error on network exception`() = runBlocking {
        `when`(mockApiService.register(any())).thenThrow(RuntimeException("Timeout"))

        val result = AuthRepository.register("test@example.com", "password123")

        assertTrue("Expected Error but got: $result", result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Network error"))
    }
}