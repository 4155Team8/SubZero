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

    // checks email format, password length, and confirmation
    private fun validateInputs(email: String, password: String, confirm: String): Boolean {
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            valid = false
        } else {
            tilEmail.error = null
        }

        val passRes = checkPassword(password)
        if (passRes.valid == false) {
            tilPassword.error = passRes.message
            valid = false
        } else tilPassword.error = null

        if (confirm != password) {
            tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else tilConfirmPassword.error = null


        return valid
    }

    // return type class for checkPassword() to bundle boolean and error message
    data class passVal(
        val valid: Boolean,
        val message: String? = null
    )

    // password regex validation stuff
    private fun checkPassword(password: String): passVal {
        if (password.length < 8)
            return passVal(false, "Password must be at least 8 characters") // check length
        if (!password.any { it.isDigit() })
            return passVal(false, "Password must contain a number") // check if it contains a numbers
        if (!password.any { it.isUpperCase() })
            return passVal(false, "Password must contain an uppercase letter") // check if it has an uppercase
        if (!password.any { it.isLowerCase() })
            return passVal(false, "Password must contain a lowercase letter") // check if it has a lowercase
        if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() })
            return passVal(false, "Password must contain one special character") // check if it has a special character (space not included)

        return passVal(true, null)
    }
    // calls registration
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

    // loading logic for feedback
    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Creating account…" else "Create Account"
    }
}
