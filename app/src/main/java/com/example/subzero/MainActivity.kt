package com.example.subzero

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
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
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.subzero.global.NotificationScheduler.scheduleReminderNotification
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
        private lateinit var tilPassword: TextInputLayout
        private lateinit var etEmail: TextInputEditText
        private lateinit var etPassword: TextInputEditText
        private lateinit var btnSignIn: MaterialButton
        private lateinit var tvDemoHint: TextView
        private lateinit var tvGoToRegister: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if the user is already logged js go to dashboard
        if (SessionManager.isLoggedIn(this)) {
            navigateToDashboard()
            return
        }

        setContentView(R.layout.activity_main)
        initViews()
        setDemoHintText()
        setupClickListeners()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        val threeDaysInMillis = TimeUnit.DAYS.toMillis(3)
        scheduleReminderNotification(
            context = this,
            title   = "Netflix renewing soon",
            body    = "Your subscription renews in 3 days",
            delayInMillis = threeDaysInMillis
        )
    }

    private fun initViews() {
        tilEmail       = findViewById(R.id.tilEmail)
        tilPassword    = findViewById(R.id.tilPassword)
        etEmail        = findViewById(R.id.etEmail)
        etPassword     = findViewById(R.id.etPassword)
        btnSignIn      = findViewById(R.id.btnSignIn)
        tvDemoHint     = findViewById(R.id.tvDemoHint)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
    }

    private fun setDemoHintText() {
        tvDemoHint.text = Html.fromHtml(
            "<b><font color='#5B2EFA'>Demo Mode:</font></b> Use any email and password",
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupClickListeners() {
        btnSignIn.setOnClickListener {
            val email    = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            if (validateInputs(email, password)) login(email, password)
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // input validation for email and password
    // checks whether email is a valid email (ie format)
    // checks password only by not empty for now (will add length and such later)
    private fun validateInputs(email: String, password: String): Boolean {
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            valid = false
        } else {
            tilEmail.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            valid = false
        } else {
            tilPassword.error = null
        }

        return valid
    }


    // calls login and saves the session
    private fun login(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            when (val result = AuthRepository.login(email, password)) {
                is AuthResult.Success -> {
                    val data = result.data
                    SessionManager.saveSession(
                        context  = this@MainActivity,
                        token    = data.token,
                        userId   = data.user.id,
                        email    = data.user.email
                    )
                    navigateToDashboard()
                }
                is AuthResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // TODO: make cool animation for signing in
    private fun setLoading(loading: Boolean) {
        btnSignIn.isEnabled = !loading
        btnSignIn.text = if (loading) "Signing in…" else "Sign In"
    }

    // navigates to insights atm, should change to dashboard once that's been constructed
    private fun navigateToDashboard() {
        startActivity(Intent(this, InsightsActivity::class.java))
    }


}