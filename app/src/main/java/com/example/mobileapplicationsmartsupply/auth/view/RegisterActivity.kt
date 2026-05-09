package com.example.mobileapplicationsmartsupply.auth.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository
import com.example.mobileapplicationsmartsupply.main.view.LoadingDialog

class RegisterActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnContinue = findViewById<Button>(R.id.btnContinue)

        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }

        btnContinue.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.etLastName).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString().trim()

            when {
                firstName.isEmpty() || lastName.isEmpty() ->
                    toast("Please enter your full name")
                email.isEmpty() ->
                    toast("Please enter your email")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast("Enter a valid email address")
                password.isEmpty() ->
                    toast("Please enter a password")
                password.length < 6 ->
                    toast("Password must be at least 6 characters")
                password != confirmPassword ->
                    toast("Passwords do not match")
                else -> {
                    btnContinue.isEnabled = false
                    LoadingDialog.show(this, "Creating your account...")

                    authRepository.register(firstName, lastName, email, password, "",
                        onSuccess = {
                            LoadingDialog.dismiss()
                            toast("Account created! Please log in.")
                            finish()
                        },
                        onError = { error ->
                            LoadingDialog.dismiss()
                            btnContinue.isEnabled = true
                            toast(error)
                        }
                    )
                }
            }
        }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener { finish() }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
