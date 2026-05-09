package com.example.mobileapplicationsmartsupply.auth.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R

class ForgotPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "OTP sent to $email", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.tvResend).setOnClickListener {
            Toast.makeText(this, "Code resent", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener { finish() }
    }
}
