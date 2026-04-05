package com.example.subzero

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.subzero.databinding.ActivityProfileBinding

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var btnBack : com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalinfo) // telling app to draw activity_insights.xml
        initViews()
        setupClickListeners()
    }

    private fun initViews() {

        btnBack      = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            back();
        }

    }
    private fun back() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}