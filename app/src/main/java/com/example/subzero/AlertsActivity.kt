package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.subzero.global.ApiCalls
import com.example.subzero.network.AlertResponse
import com.example.subzero.network.ApiClient
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import com.example.subzero.global.Utility
import com.example.subzero.global.NotificationScheduler
class AlertsActivity : AppCompatActivity() {
    private lateinit var remindersContainer : androidx.cardview.widget.CardView
    private lateinit var remindersList : LinearLayout
    private lateinit var tvNewAlerts : TextView
    private lateinit var btnClearAll: TextView

    private var util = Utility()
    private var calls = ApiCalls()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts) // telling app to draw activity_alerts.xml
        initViews() // initiates views
        setupBottomNav()
        loadAlerts()

        //set the click listener for bulk action
        btnClearAll.setOnClickListener{
            handleClearAll()
        }
    }

    private fun initViews() {
        // TODO: CONVERT TO LATEINIT
        val navManage = findViewById<LinearLayout>(R.id.navManage)
        val navInsights = findViewById<LinearLayout>(R.id.navInsights)
        val navAlerts = findViewById<LinearLayout>(R.id.navAlerts)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        remindersContainer = findViewById(R.id.remindersContainer)
        remindersList = findViewById(R.id.remindersList)
        tvNewAlerts = findViewById(R.id.tvNewAlerts)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { /* nothing yet */ }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { navigateToInsights() }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener { /* already here */ }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { navigateToProfile() }
    }
    private fun renderAlerts(alerts: List<AlertResponse>) {
        remindersList.removeAllViews()

        val dp = resources.displayMetrics.density
        alerts.forEach { alert ->
            val alertView = buildAlerts(alert, dp)
            remindersList.addView(alertView)
        }
    }
    private fun loadAlerts() {
        val token = SessionManager.getToken(this) ?: return

        lifecycleScope.launch {

                // grab alerts
                val alerts = calls.loadReminders(this@AlertsActivity)

                // create list
                val reminders: List<AlertResponse> = alerts ?: emptyList()

                // rendering stuff
                if (reminders.isEmpty()) {
                    showEmptyState()
                } else {
                    tvNewAlerts.text = reminders.size.toString() + " new"
                    renderAlerts(reminders)
                    scheduleNotificationsForAlerts(reminders)
                }
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
        btnClearAll.visibility = View.GONE

        //Placeholder text
        val emptyTextView = TextView(this).apply {
            text = "No new alerts. You are all caught up!"
            gravity = android.view.Gravity.CENTER
            setPadding(0,(50 * resources.displayMetrics.density).toInt(),0,0)
        }
        remindersList.addView(emptyTextView)
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
        // nothing yet
    }
    private fun handleClearAll(){
        //show a confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear Notifications")
            .setMessage("Are you sure you want to clear all notifications?")
            .setPositiveButton("Clear All") { _, _ ->
                performBulkDelete()
                //call the api
                }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun performBulkDelete() {
        lifecycleScope.launch {
            val success = calls.clearAllAlerts(this@AlertsActivity)

            if (success) {
                //update the UI locally
                remindersList.removeAllViews()
                tvNewAlerts.text = "0 new"
                showEmptyState()
                Toast.makeText(this@AlertsActivity, "Alerts cleared", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AlertsActivity, "Failed to clear alerts", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    
}
