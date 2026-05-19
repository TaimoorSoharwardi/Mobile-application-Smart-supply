package com.example.mobileapplicationsmartsupply.main.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository

class EditProfileActivity : BaseActivity() {

    private val authRepository = AuthRepository()
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        btnSave = findViewById(R.id.btnSave)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadCurrentProfile()

        btnSave.setOnClickListener { saveProfile() }
    }

    private fun loadCurrentProfile() {
        // Show email immediately from Firebase Auth
        val emailView = findViewById<TextView>(R.id.tvEditEmail)
        emailView.text = authRepository.getCurrentUserEmail()

        // Then load full profile from Firestore
        authRepository.getCurrentUser(
            onSuccess = { user ->
                emailView.text = user.email.ifEmpty { authRepository.getCurrentUserEmail() }
                findViewById<EditText>(R.id.etFirstName).setText(user.firstName)
                findViewById<EditText>(R.id.etLastName).setText(user.lastName)
                findViewById<EditText>(R.id.etPhone).setText(user.phone)
                findViewById<EditText>(R.id.etLocation).setText(user.location)
                findViewById<EditText>(R.id.etCompany).setText(user.company)
            },
            onError = { /* email already set as fallback above */ }
        )
    }

    private fun saveProfile() {
        val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.etLastName).text.toString().trim()
        val phone = findViewById<EditText>(R.id.etPhone).text.toString().trim()
        val location = findViewById<EditText>(R.id.etLocation).text.toString().trim()
        val company = findViewById<EditText>(R.id.etCompany).text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            toast("First and last name are required")
            return
        }

        btnSave.isEnabled = false
        LoadingDialog.show(this, "Saving profile...")

        authRepository.updateUserProfile(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            location = location,
            company = company,
            onSuccess = {
                LoadingDialog.dismiss()
                toast("Profile updated successfully")
                finish()
            },
            onError = { error ->
                LoadingDialog.dismiss()
                btnSave.isEnabled = true
                toast(error)
            }
        )
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
