package com.example.subzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.subzero.ui.theme.SubZeroTheme

/**
 * Host activity for the Compose-based subscription management screen.
 */
class ManagementActivity : ComponentActivity() {

    private val vm: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SubZeroTheme {
                SubscriptionManagementScreen(
                    vm = vm,
                    onGoToDashboard = { finish() }
                )
            }
        }
    }
}
