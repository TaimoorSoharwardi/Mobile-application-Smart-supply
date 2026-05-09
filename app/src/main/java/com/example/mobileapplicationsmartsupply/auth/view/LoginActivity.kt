package com.example.mobileapplicationsmartsupply.auth.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository
import com.example.mobileapplicationsmartsupply.main.view.HomeActivity
import com.example.mobileapplicationsmartsupply.main.view.LoadingDialog

class LoginActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                email.isEmpty() ->
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                password.isEmpty() ->
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                else -> {
                    btnLogin.isEnabled = false
                    btnLogin.alpha = 0.7f
                    LoadingDialog.show(this, "Logging in...")

                    authRepository.login(email, password,
                        onSuccess = {
                            LoadingDialog.dismiss()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        },
                        onError = { error ->
                            LoadingDialog.dismiss()
                            btnLogin.isEnabled = true
                            btnLogin.alpha = 1f
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
