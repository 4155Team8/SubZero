package com.example.subzero

import android.app.AlertDialog
import android.app.Person
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
import com.example.subzero.databinding.ActivityProfileBinding
import com.example.subzero.network.ApiClient
import com.example.subzero.network.SubscriptionResponse
import com.example.subzero.global.Utility
import android.util.Log
import android.widget.Button
import com.example.subzero.global.ApiCalls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnSignOut: LinearLayout
    private lateinit var binding: ActivityProfileBinding
    private lateinit var tvAvatarInitials : TextView
    private lateinit var tvUserName : TextView
    private lateinit var tvUserEmail : TextView
    private lateinit var btnPersonalInfo : LinearLayout
    private lateinit var btnDeleteAcc : LinearLayout
    private var util = Utility()
    private var calls = ApiCalls()

    data class UserProfile(
        val fullName: String?,
        val email: String?,
        val memberSince: String?,
        val numSubs: Int?,
        val notificationsEnabled: Boolean?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupBottomNav()
        setupClickListeners()
        loadProfileData()
    }
    private fun initViews() {

        btnSignOut = findViewById(R.id.btnSignOut)
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnPersonalInfo = findViewById(R.id.btnPersonalInfo)
        btnDeleteAcc = findViewById(R.id.btnDeleteAcc)
    }


    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { navigateToDashboard() }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { navigateToInsights() }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener { navigateToAlerts() }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { /* already here */ }
    }
    private fun setupClickListeners() {
        btnSignOut.setOnClickListener {
            logout()
        }
        btnPersonalInfo.setOnClickListener {
            navigateToPersonalInfo()
        }
        btnDeleteAcc.setOnClickListener {
            showDeleteConfirmationDialog()
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

    private fun loadProfileData() {
        val token = SessionManager.getToken(this) ?: return

        lifecycleScope.launch {
            val profile = calls.loadProfile(this@ProfileActivity)
            val dashboard = calls.loadDashboard(this@ProfileActivity)
            Log.d("current email", ": " + profile?.email)
            val email = profile?.email ?: "Not available"
            val name = profile?.name ?: "John Doe"
            val remindersEnabled = profile?.reminders_enabled == 1
            val memberSince = profile?.created_at
            val subs: List<SubscriptionResponse> = dashboard?.subscriptions ?: emptyList()
            val numSubs = subs.size

            val userProfile = UserProfile(
                fullName = name,
                email = email,
                memberSince = util.formatToMonthYear(memberSince),
                numSubs = numSubs,
                notificationsEnabled = remindersEnabled
            )

            binding.tvUserName.text = userProfile.fullName
            binding.tvUserEmail.text = userProfile.email
            binding.tvMemberSinceValue.text = userProfile.memberSince
            binding.tvTotalSubs.text = userProfile.numSubs.toString()

        }
    }

    private fun navigateToInsights() {
        startActivity(Intent(this, InsightsActivity::class.java))
    }
    private fun navigateToAlerts() {
        startActivity(Intent(this, AlertsActivity::class.java))
    }
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }
    private fun navigateToPersonalInfo() {
        startActivity(Intent(this, PersonalInfoActivity::class.java))
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAcc() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAcc() {
        val token = SessionManager.getToken(this) ?: return
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                calls.deleteAccount(this@ProfileActivity)
            }
            if (result != null) {
                SessionManager.clearSession(this@ProfileActivity)
                startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@ProfileActivity, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
        }

    }
}