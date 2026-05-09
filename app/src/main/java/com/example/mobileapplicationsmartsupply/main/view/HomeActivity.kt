package com.example.mobileapplicationsmartsupply.main.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository

class HomeActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        BottomNavHelper.setup(this, BottomNavHelper.TAB_HOME)

        loadUserName()

        findViewById<LinearLayout>(R.id.btnRequestNew).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardKitchen).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java).putExtra("category", "Kitchen"))
        }

        findViewById<LinearLayout>(R.id.cardStationary).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java).putExtra("category", "Stationary"))
        }

        findViewById<LinearLayout>(R.id.cardFurniture).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java).putExtra("category", "Furniture"))
        }

        findViewById<LinearLayout>(R.id.cardEvents).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java).putExtra("category", "Events"))
        }
    }

    private fun loadUserName() {
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Show email as fallback immediately while Firestore loads
        val email = authRepository.getCurrentUserEmail()
        if (email.isNotEmpty()) {
            tvUserName.text = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        // Then fetch full name from Firestore and update
        authRepository.getCurrentUser(
            onSuccess = { user ->
                val name = "${user.firstName} ${user.lastName}".trim()
                tvUserName.text = name.ifEmpty {
                    email.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
            },
            onError = { /* keep the email-based fallback */ }
        )
    }
}
