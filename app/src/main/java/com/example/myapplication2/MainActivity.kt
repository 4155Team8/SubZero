package com.example.myapplication2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication2.ui.theme.MyApplication2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplication2Theme {
                val viewModel = remember { SubscriptionViewModel() }
                var currentScreen by remember { mutableStateOf("dashboard") }

                when (currentScreen) {
                    "dashboard" -> SubscriptionDashboardScreen(
                        vm = viewModel,
                        onGoToManage = { currentScreen = "manage" }
                    )

                    "manage" -> SubscriptionManagementScreen(
                        vm = viewModel,
                        onGoToDashboard = { currentScreen = "dashboard" }
                    )
                }
            }
        }
    }
}