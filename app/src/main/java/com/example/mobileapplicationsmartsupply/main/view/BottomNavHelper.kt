package com.example.mobileapplicationsmartsupply.main.view

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mobileapplicationsmartsupply.R

object BottomNavHelper {

    const val TAB_HOME = 0
    const val TAB_REQUEST = 1
    const val TAB_PROFILE = 2

    fun setup(activity: AppCompatActivity, activeTab: Int) {
        val goldColor = ContextCompat.getColor(activity, R.color.gold)
        val whiteColor = ContextCompat.getColor(activity, R.color.white)

        val navHome = activity.findViewById<LinearLayout>(R.id.navHome)
        val navRequest = activity.findViewById<LinearLayout>(R.id.navRequest)
        val navProfile = activity.findViewById<LinearLayout>(R.id.navProfile)

        val navHomeIcon = activity.findViewById<ImageView>(R.id.navHomeIcon)
        val navRequestIcon = activity.findViewById<ImageView>(R.id.navRequestIcon)
        val navProfileIcon = activity.findViewById<ImageView>(R.id.navProfileIcon)

        val navHomeText = activity.findViewById<TextView>(R.id.navHomeText)
        val navRequestText = activity.findViewById<TextView>(R.id.navRequestText)
        val navProfileText = activity.findViewById<TextView>(R.id.navProfileText)

        fun setActive(icon: ImageView, text: TextView, active: Boolean) {
            val color = if (active) goldColor else whiteColor
            icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            text.setTextColor(color)
        }

        setActive(navHomeIcon, navHomeText, activeTab == TAB_HOME)
        setActive(navRequestIcon, navRequestText, activeTab == TAB_REQUEST)
        setActive(navProfileIcon, navProfileText, activeTab == TAB_PROFILE)

        navHome.setOnClickListener {
            if (activeTab != TAB_HOME) {
                val intent = Intent(activity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        navRequest.setOnClickListener {
            if (activeTab != TAB_REQUEST) {
                val intent = Intent(activity, OrdersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        navProfile.setOnClickListener {
            if (activeTab != TAB_PROFILE) {
                val intent = Intent(activity, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }
}
