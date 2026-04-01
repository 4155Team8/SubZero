package com.example.subzero

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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

            if (!validateEmail(email)) return@setOnClickListener

            // Hide keyboard, show confirmation message
            etEmail.clearFocus()
            showConfirmation()

            // TODO: hook up to your backend password reset endpoint when ready
            // e.g. POST /auth/forgot-password { email }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            false
        } else {
            tilEmail.error = null
            true
        }
    }

    private fun showConfirmation() {
        // Disable confirm button so it can't be spammed
        btnConfirm.isEnabled = false
        btnConfirm.text = "Sent"

        tvConfirmationMessage.visibility = View.VISIBLE
    }
}
