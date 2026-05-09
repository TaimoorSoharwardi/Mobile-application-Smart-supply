package com.example.mobileapplicationsmartsupply.main.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository
import com.example.mobileapplicationsmartsupply.auth.view.LoginActivity

class ProfileActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        BottomNavHelper.setup(this, BottomNavHelper.TAB_PROFILE)

        loadUserProfile()

        findViewById<LinearLayout>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnNotifications).setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            authRepository.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        authRepository.getCurrentUser(
            onSuccess = { user ->
                // Update email row
                val tvEmail = findTextViewByPosition(0)
                tvEmail?.text = user.email.ifEmpty { authRepository.getCurrentUserEmail() }

                // Update phone row
                val tvPhone = findTextViewByPosition(1)
                tvPhone?.text = user.phone.ifEmpty { "Not set" }

                // Update location row
                val tvLocation = findTextViewByPosition(2)
                tvLocation?.text = user.location.ifEmpty { "Not set" }
            },
            onError = { /* Keep static placeholder values */ }
        )
    }

    // Helper to find the value TextViews in the profile info rows
    private fun findTextViewByPosition(index: Int): TextView? {
        val ids = listOf(
            R.id.tvProfileEmail,
            R.id.tvProfilePhone,
            R.id.tvProfileLocation
        )
        return if (index < ids.size) findViewById(ids[index]) else null
    }
}
