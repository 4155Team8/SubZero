package com.example.subzero

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.subzero.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    data class UserProfile(
        val fullName: String,
        val email: String,
        val memberSince: String,
        val totalSaved: String,
        val notificationsEnabled: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupClickListeners()
        loadProfileData()
    }

    private fun setupNavigation() {
        // Since we are in ProfileActivity, highlight the Profile nav item
        binding.navProfile.isSelected = true

        binding.navManage.setOnClickListener {
            Toast.makeText(this, "Navigating to Manage...", Toast.LENGTH_SHORT).show()
        }

        binding.navInsights.setOnClickListener {
            Toast.makeText(this, "Navigating to Insights...", Toast.LENGTH_SHORT).show()
        }

        binding.navAlerts.setOnClickListener {
            Toast.makeText(this, "Navigating to Alerts...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnPersonalInfo.setOnClickListener {
            Toast.makeText(this, "Opening Personal Information...", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Opening Notification Settings...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileData() {
        // Simulate data from a repository or database
        val profile = UserProfile(
            fullName = "John Doe",
            email = "john.doe@email.com",
            memberSince = "Jan 2024",
            totalSaved = "$127.50",
            notificationsEnabled = true
        )

        binding.tvUserName.text = profile.fullName
        binding.tvUserEmail.text = profile.email
        binding.tvMemberSinceValue.text = profile.memberSince
        binding.tvTotalSavedValue.text = profile.totalSaved
        
        // Update avatar initials
        val initials = profile.fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
        binding.tvAvatarInitials.text = initials
    }
}
