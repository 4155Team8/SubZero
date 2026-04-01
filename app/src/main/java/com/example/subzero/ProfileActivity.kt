package com.example.subzero;

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.LinearLayout
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

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnSignOut: MaterialButton;

    // uses onCreate and sets up the view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initViews()
        setupBottomNav()
        setupClickListeners()
    }

    // initiate views
    private fun initViews() {

        btnSignOut      = findViewById(R.id.btnSignOut)

    }

    // literally just the logout function for when the logout button is clicked
    private fun setupClickListeners() {
        btnSignOut.setOnClickListener {
            logout();
        }

    }

    // renders bottom nav bar
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { /* nothing yet */ }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { navigateToInsights() }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            // no alerts page yet
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            /* already here*/
        }
    }

    private fun logout() {
        // clears the jwt token and info for the user
        SessionManager.clearSession(this)

        // create variable called intent with the main page as the page to go to
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // navigate there
        startActivity(intent)
        finish()
    }
    private fun navigateToInsights() {
        startActivity(Intent(this, InsightsActivity::class.java))
    }
}

