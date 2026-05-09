package com.example.mobileapplicationsmartsupply.auth.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository
import com.example.mobileapplicationsmartsupply.main.view.HomeActivity

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val authRepository = AuthRepository()

        Handler(Looper.getMainLooper()).postDelayed({
            if (authRepository.isLoggedIn()) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
