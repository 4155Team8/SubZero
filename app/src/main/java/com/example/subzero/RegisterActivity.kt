package com.example.subzero

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.network.AuthRepository
import com.example.subzero.network.AuthResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tilEmail           = findViewById(R.id.tilEmail)
        tilPassword        = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etEmail            = findViewById(R.id.etEmail)
        etPassword         = findViewById(R.id.etPassword)
        etConfirmPassword  = findViewById(R.id.etConfirmPassword)
        btnRegister        = findViewById(R.id.btnRegister)
        tvGoToLogin        = findViewById(R.id.tvGoToLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val email    = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            val confirm  = etConfirmPassword.text?.toString() ?: ""
            if (validateInputs(email, password, confirm)) performRegister(email, password)
        }

        tvGoToLogin.setOnClickListener { finish() }
    }

    private fun validateInputs(email: String, password: String, confirm: String): Boolean {
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            valid = false
        } else {
            tilEmail.error = null
        }

        if (password.length < 8) {
            tilPassword.error = "Password must be at least 8 characters"
            valid = false
        } else {
            tilPassword.error = null
        }

        if (confirm != password) {
            tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else {
            tilConfirmPassword.error = null
        }

        return valid
    }

    private fun performRegister(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            when (val result = AuthRepository.register(email, password)) {
                is AuthResult.Success -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created! Please sign in.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Go back to login
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    finish()
                }
                is AuthResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Creating account…" else "Create Account"
    }
}
