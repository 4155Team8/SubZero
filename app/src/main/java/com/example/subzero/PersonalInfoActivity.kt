package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.RegisterActivity.passVal
import com.example.subzero.databinding.ActivityProfileBinding
import com.example.subzero.global.ApiCalls
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import com.google.android.material.textfield.TextInputLayout


class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var btnBack : com.google.android.material.button.MaterialButton
    private lateinit var etName : com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSaveChanges : com.google.android.material.button.MaterialButton
    private lateinit var etEmail : com.google.android.material.textfield.TextInputEditText
    private lateinit var etPassword : com.google.android.material.textfield.TextInputEditText
    private lateinit var tvConfirmation : TextView
    private lateinit var ivPassMeter : ImageView
    private lateinit var tvPassStrength : TextView
    private lateinit var tilPassword : TextInputLayout
    private lateinit var tvPassError : TextView
    private val api = ApiCalls()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalinfo) // telling app to draw activity_insights.xml
        initViews()
        setupClickListeners()
    }

    private fun initViews() {

        btnBack      = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvConfirmation = findViewById(R.id.tvConfirmation)
        tvPassStrength = findViewById(R.id.tvPassStrength)
        ivPassMeter = findViewById(R.id.ivPassMeter)
        tilPassword = findViewById(R.id.tilPassword)
        tvPassError = findViewById(R.id.tvPassError)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            back();
        }
        btnSaveChanges.setOnClickListener {
            if (!etName.text.isNullOrEmpty()) {
                routeLogic(etName.text.toString(), null, null)
            }
            if (!etEmail.text.isNullOrEmpty()) {
                Log.d("Email3", "What's in the box: " + etEmail.text.toString())
                routeLogic(null, etEmail.text.toString(), null)
            }
            if (!etPassword.text.isNullOrEmpty() && validateInputs(etPassword.text.toString().trim())) {
                routeLogic(null,null, etPassword.text.toString())
            }
        }

    }

    private fun routeLogic(name: String?, email: String?, password: String?) {
        if (name != null) {
            val newName = etName.text?.toString()?.trim() ?: ""
            setLoading(true)
            lifecycleScope.launch {
                val result = api.updateName(this@PersonalInfoActivity, name)
                val updatedName = result?.user?.name
                if (newName == updatedName) {
                    tvConfirmation.setTextColor(Color.argb(255,69,83,226))
                    setLoading(false)
                }
            }
        }
        if (email != null) {
            val newEmail    = etEmail.text?.toString()?.trim() ?: ""
            setLoading(true)
            lifecycleScope.launch {
                val result = api.updateEmail(this@PersonalInfoActivity, email)
                val updatedEmail = result?.user?.email
                Log.d("Email1", "API response: " + updatedEmail)
                Log.d("Email2", "User input: " + newEmail)
                if (newEmail == updatedEmail) {
                    SessionManager.saveSession(
                        context = this@PersonalInfoActivity,
                        token = SessionManager.getToken(this@PersonalInfoActivity) ?: "",
                        userId = SessionManager.getUserId(this@PersonalInfoActivity),
                        email = updatedEmail,
                    )
                    tvConfirmation.setTextColor(Color.argb(255,69,83,226))
                    setLoading(false)
                }
            }
        }
        if (password != null) {
            val newPassword = etPassword.text?.toString() ?: ""
            setLoading(true)
            lifecycleScope.launch {
                val result = api.updatePassword(this@PersonalInfoActivity, password)
                val newHash = result?.user?.password_hash
                if (result?.user?.password_hash != null) {
                    tvConfirmation.setTextColor(Color.argb(255,69,83,226))
                    setLoading(false)
                }
            }
        }
    }
    private fun setLoading(loading: Boolean) {
        btnSaveChanges.isEnabled = !loading
        btnSaveChanges.text = if (loading) "Saving Changes" else "Save Changes"
    }
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
    private fun validateInputs(password: String): Boolean {
        var valid = true

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
        return valid
    }
    private fun back() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}