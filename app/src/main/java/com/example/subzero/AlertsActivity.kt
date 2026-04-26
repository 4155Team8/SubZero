package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.subzero.global.ApiCalls
import com.example.subzero.network.AlertResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.subzero.global.Utility
import com.example.subzero.global.NotificationScheduler

class AlertsActivity : AppCompatActivity() {
    private lateinit var remindersContainer: CardView
    private lateinit var remindersList: LinearLayout
    private lateinit var tvNewAlerts: TextView
    private lateinit var btnClearAll: TextView
    private lateinit var rlNotificationBackground: RelativeLayout
    private lateinit var tvNotificationStatus: TextView
    private lateinit var tvNotificationDesc: TextView
    private lateinit var ivBellIcon: ImageView

    private var util = Utility()
    private var calls = ApiCalls()
    private var remindersEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)
        initViews()
        setupBottomNav()
        loadProfileAndAlerts()
    }

    private fun initViews() {
        remindersContainer = findViewById(R.id.remindersContainer)
        remindersList = findViewById(R.id.remindersList)
        tvNewAlerts = findViewById(R.id.tvNewAlerts)
        btnClearAll = findViewById(R.id.btnClearAll)
        rlNotificationBackground = findViewById(R.id.rlNotificationBackground)
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus)
        tvNotificationDesc = findViewById(R.id.tvNotificationDesc)
        ivBellIcon = findViewById(R.id.ivBellIcon)
        setupClearAll()
    }

    private fun setupClearAll() {
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Notifications")
                .setMessage("Are you sure you want to clear all notifications?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        val success = calls.clearReminders(this@AlertsActivity)
                        if (success) {
                            remindersList.removeAllViews()
                            tvNewAlerts.text = "0 new"
                            Toast.makeText(this@AlertsActivity, "Alerts cleared", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AlertsActivity, "Failed to clear alerts", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { navigateToDashboard() }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { navigateToInsights() }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener { /* already here */ }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { navigateToProfile() }
    }

    private fun applyNotificationState(enabled: Boolean) {
        remindersEnabled = enabled
        if (enabled) {
            rlNotificationBackground.setBackgroundColor(Color.parseColor("#5a42f5"))
            tvNotificationStatus.text = "Notifications Enabled"
            tvNotificationDesc.text = "You'll be notified 3 days before renewals"
            ivBellIcon.alpha = 1f
        } else {
            rlNotificationBackground.setBackgroundResource(R.drawable.gradient_background_disabled)
            tvNotificationStatus.text = "Notifications Disabled"
            tvNotificationDesc.text = "Tap to re-enable renewal reminders"
            ivBellIcon.alpha = 0.5f
        }
    }

    private fun setupNotificationToggle() {
        rlNotificationBackground.setOnClickListener {
            val newState = !remindersEnabled
            // Optimistically update UI
            applyNotificationState(newState)
            // Persist to API
            lifecycleScope.launch {
                val result = calls.updateNotifications(this@AlertsActivity, newState)
                if (result == null) {
                    // Revert on failure
                    applyNotificationState(!newState)
                    Toast.makeText(this@AlertsActivity, "Failed to update notification setting", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProfileAndAlerts() {
        lifecycleScope.launch {
            // Load profile to get reminders_enabled preference
            val profile = calls.loadProfile(this@AlertsActivity)
            val enabled = profile?.reminders_enabled == 1
            applyNotificationState(enabled)
            setupNotificationToggle()

            // Load alerts
            val alerts = calls.loadReminders(this@AlertsActivity)
            val reminders: List<AlertResponse> = alerts ?: emptyList()
            if (reminders.isEmpty()) {
                showEmptyState()
            } else {
                tvNewAlerts.text = "${reminders.size} new"
                renderAlerts(reminders)
                if (enabled) {
                    scheduleNotificationsForAlerts(reminders)
                }
            }
        }
    }
    private fun renderAlerts(alerts: List<AlertResponse>) {
        remindersList.removeAllViews()
        val dp = resources.displayMetrics.density
        alerts.forEach { alert ->
            remindersList.addView(buildAlerts(alert, dp))
        }
    }

    private fun scheduleNotificationsForAlerts(alerts: List<AlertResponse>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        alerts.forEach { alert ->
            try {
                val reminderDate = sdf.parse(alert.reminder_date) ?: return@forEach
                val delay = reminderDate.time - System.currentTimeMillis()
                if (delay > 0) {
                    NotificationScheduler.scheduleReminderNotification(
                        context      = this,
                        title        = alert.name,
                        body         = alert.description,
                        delayInMillis = delay
                    )
                }
            } catch (e: Exception) {
                Log.e("AlertsActivity", "Failed to schedule notification: ${e.message}")
            }
        }
    }


    private fun showEmptyState() {
        // TODO: ADD EMPTY PLACEHOLDERS
    }

    private fun buildAlerts(alert: AlertResponse, dp: Float): View {
        // Card container for each alert
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.topMargin = (16 * dp).toInt() // spacing between cards
            }
            radius = 16 * dp
            cardElevation = 2 * dp
            useCompatPadding = true
            setPadding((24 * dp).toInt(), 0, 0, 0) // left indent
        }

        // Content inside the card
        val content = RelativeLayout(this).apply {
            val padding = (16 * dp).toInt()
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(padding, padding, padding, padding)
        }

        // Icon
        val ivIcon = ImageView(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams((40 * dp).toInt(), (40 * dp).toInt())
            setBackgroundResource(R.drawable.bg_circle_light_blue)
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            setImageResource(R.drawable.ic_calendar)
            setColorFilter(ContextCompat.getColor(context, R.color.blue_icon))
        }
        content.addView(ivIcon)

        // Title
        val tvTitle = TextView(this).apply {
            id = View.generateViewId()
            text = alert.name
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, ivIcon.id)
                marginStart = (12 * dp).toInt()
            }
        }
        content.addView(tvTitle)

        // Time
        val tvTime = TextView(this).apply {
            id = View.generateViewId()
            text = util.timeAgo(alert.reminder_date)
            setTextColor(ContextCompat.getColor(context, R.color.gray_text))
            textSize = 12f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
        }
        content.addView(tvTime)

        // Subtitle
        val tvSubtitle = TextView(this).apply {
            id = View.generateViewId()
            text = alert.description
            setTextColor(ContextCompat.getColor(context, R.color.gray_text))
            textSize = 13f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, tvTitle.id)
                addRule(RelativeLayout.END_OF, ivIcon.id)
                topMargin = (4 * dp).toInt()
                marginStart = (12 * dp).toInt()
            }
        }
        content.addView(tvSubtitle)

        // Add content to card
        card.addView(content)

        return card
    }
    private fun navigateToInsights() {
        startActivity(Intent(this, InsightsActivity::class.java))
    }
    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }
}