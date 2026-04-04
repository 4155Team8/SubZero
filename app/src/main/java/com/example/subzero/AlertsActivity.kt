package com.example.subzero

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AlertsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        //Bottom Navigation Buttons
        val navManage = findViewById<LinearLayout>(R.id.navManage)
        val navInsights = findViewById<LinearLayout>(R.id.navInsights)
        val navAlerts = findViewById<LinearLayout>(R.id.navAlerts)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        //Navigation clicks
        navManage.setOnClickListener{
            //Navigate to Manage Screen
            Toast.makeText(this, "Manage clicked", Toast.LENGTH_SHORT).show()
        }
        navInsights.setOnClickListener{
            //Navigate to Insights Screen
            navigateToInsights()
        }
        navAlerts.setOnClickListener{
            //Navigate to Alerts Screen
            Toast.makeText(this, "Already on Alerts", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener{
            //Navigate to Profile Screen
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        //Example
        val netflixTitle = findViewById<TextView>(R.id.tvNetflixTitle)
        val netflixTime = findViewById<TextView>(R.id.tvNetflixTime)

        val warningTitle = findViewById<TextView>(R.id.tvWarningTitle)
        warningTitle.setOnClickListener{
            Toast.makeText(this, "Price has increased", Toast.LENGTH_SHORT).show()
        }




    }
    private fun navigateToInsights(){
        val intent = Intent(this, InsightsActivity::class.java)
        startActivity(intent)
    }

}