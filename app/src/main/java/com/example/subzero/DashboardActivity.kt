package com.example.subzero

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.subzero.ui.theme.SubZeroTheme

/**
 * Host activity for the Compose-based subscription dashboard.
 * MainActivity navigates here after a successful login.
 */
class DashboardActivity : ComponentActivity() {

    private val vm: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SubZeroTheme {
                SubscriptionDashboardScreen(
                    vm = vm,
                    onGoToManage = {
                        startActivity(Intent(this, ManagementActivity::class.java))
                    },
                    onGoToInsights = {
                        startActivity(Intent(this, InsightsActivity::class.java))
                    },
                    onGoToAlerts = {
                        startActivity(Intent(this, AlertsActivity::class.java))
                    },
                    onGoToProfile = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                )
            }
        }
    }
}
