package com.example.subzero

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.network.ApiClient
import com.example.subzero.network.ForgotPasswordRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvConfirmationMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tilEmail              = findViewById(R.id.tilEmail)
        etEmail               = findViewById(R.id.etEmail)
        btnConfirm            = findViewById(R.id.btnConfirm)
        btnCancel             = findViewById(R.id.btnCancel)
        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage)
    }

    private fun setupClickListeners() {
        btnConfirm.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            if (validateEmail(email)) performForgotPassword(email)
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    // validation thing for emails
    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            false
        } else {
            tilEmail.error = null
            true
        }
    }

    private fun performForgotPassword(email: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.forgotPassword(ForgotPasswordRequest(email))

                // will always show error 200 so people cant brute force to see which emails r registered
                setLoading(false)
                showConfirmation()

            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnConfirm.isEnabled = !loading
        btnConfirm.text = if (loading) "Sending…" else "Confirm"
    }

    private fun showConfirmation() {
        btnConfirm.isEnabled = false
        btnConfirm.text = "Sent"
        etEmail.isEnabled = false
        tvConfirmationMessage.visibility = View.VISIBLE
    }
}