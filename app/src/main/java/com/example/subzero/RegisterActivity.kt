package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
import org.w3c.dom.Text
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvGoToLogin: TextView
    private lateinit var tvPassError : TextView
    private lateinit var tvPassStrength : TextView
    private lateinit var ivPassMeter : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tilEmail           = findViewById(R.id.tilEmail)
        tilPassword        = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etEmail            = findViewById(R.id.etEmail)
        etPassword         = findViewById(R.id.etPassword)
        etConfirmPassword  = findViewById(R.id.etConfirmPassword)
        btnRegister        = findViewById(R.id.btnRegister)
        tvGoToLogin        = findViewById(R.id.tvGoToLogin)
        tvPassError        = findViewById(R.id.tvPassError)
        tvPassStrength     = findViewById(R.id.tvPassStrength)
        ivPassMeter        = findViewById(R.id.ivPassMeter)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val email    = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            val confirm  = etConfirmPassword.text?.toString() ?: ""
            if (validateInputs(email, password, confirm)) performRegister(email, password)
        }

        tvGoToLogin.setOnClickListener { finish() }
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                if (password.isEmpty()) {
                    tvPassStrength.visibility = View.GONE
                    ivPassMeter.visibility = View.GONE
                    return
                }
                tvPassStrength.visibility = View.VISIBLE
                ivPassMeter.visibility = View.VISIBLE
                val result = checkPassword(password)
                tvPassStrength.text = "Password strength: ${result.strength}"
            }
        })
    }

    // checks email format, password length, and confirmation
    private fun validateInputs(email: String, password: String, confirm: String): Boolean {
        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email address"
            valid = false
        } else {
            tilEmail.error = null
        }


        // TODO: gonna put all this stuff in a wrapper function for readability
        val passRes = checkPassword(password)
        if (passRes.valid == false) {
            tvPassError.text = "Password strength guidelines: \n"
            tvPassStrength.text = "Password strength: "
            tvPassError.setVisibility(View.VISIBLE)
            tvPassStrength.setVisibility(View.VISIBLE)
            passRes.messages?.forEach {
                tvPassError.append(it)
            }
            tvPassStrength.append(passRes.strength.toString())
            tilPassword.error = "Password too weak"
            valid = false
        } else {
            tvPassError.setVisibility(View.GONE)
            tvPassStrength.text = "Password strength: "
            tvPassStrength.setVisibility(View.VISIBLE)
            tvPassStrength.append(passRes.strength.toString())
            tilPassword.error = null
            tilPassword.isErrorEnabled = false
        }

        if (confirm != password) {
            tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else tilConfirmPassword.error = null


        return valid
    }

    // return type class for checkPassword() to bundle boolean and error message
    data class passVal(
        var valid: Boolean?,
        var messages: Array<String>? = Array(5) { "" },
        var strength: Int?
    )

    // password regex validation stuff
    private fun checkPassword(password: String): passVal {
        var passStrength = 5
        val curr = passVal(
            valid = false,
            strength = 0
        )
        if (password.length < 8) {
            passStrength -= 3
            curr.messages?.set(0, "• Make the password longer\n")  // check length
        }
        if (!password.any { it.isDigit() }) {
            passStrength -= 1
            curr.messages?.set(1, "• Include a number\n")  // check if it contains a numbers
        }

        if (!password.any { it.isUpperCase() }) {
            passStrength -= 1
            curr.messages?.set(2, "• Include an uppercase letter\n")  // check if it has an uppercase
        }

        if (!password.any { it.isLowerCase() }) {
            passStrength -= 1
            curr.messages?.set(3, "• Include a lowercase letter\n") // check if it has a lowercase
        }

        if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) {
            passStrength -= 1
            curr.messages?.set(4, "• Include a special character (e.g, ! $ ? @)") // check if it has a special character (space not included)
        }


        // all of this stuff is password strength visual cues like the meter and changing password strength colors
        // TODO: possibly more efficient handling instead of tons of if statements
        if (passStrength == 0) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter0)
            tvPassStrength.setTextColor(Color.parseColor("#f70000"))
            curr.valid = false
        }
        if (passStrength == 1) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter1)
            tvPassStrength.setTextColor(Color.parseColor("#f70000"))
            curr.valid = false
        }
        if (passStrength == 2) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter2)
            tvPassStrength.setTextColor(Color.parseColor("#f70000"))
            curr.valid = false
        }
        if (passStrength == 3) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter3)
            tvPassStrength.setTextColor(Color.parseColor("#969603"))
            curr.valid = true
        }
        if (passStrength == 4) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter4)
            tvPassStrength.setTextColor(Color.parseColor("#027d05"))
            curr.valid = true
        }
        if (passStrength == 5) {
            ivPassMeter.setImageResource(R.drawable.ic_passmeter5)
            tvPassStrength.setTextColor(Color.parseColor("#027d05"))
            curr.valid = true
        }
        curr.strength = passStrength
        return curr
    }
    // calls registration
    private fun performRegister(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            when (val result = AuthRepository.register(email, password)) {
                is AuthResult.Success -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created! Please sign in.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Go back to login
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    finish()
                }
                is AuthResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // loading logic for feedback
    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Creating account…" else "Create Account"
    }
}
