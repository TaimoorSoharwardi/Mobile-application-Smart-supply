package com.example.mobileapplicationsmartsupply.main.view

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.mobileapplicationsmartsupply.R

object LoadingDialog {

    private var dialog: AlertDialog? = null

    fun show(context: Context, message: String = "Please wait...") {
        if (dialog?.isShowing == true) return

        val gold = context.getColor(R.color.gold)
        val white = context.getColor(R.color.white)
        val cardBg = context.getColor(R.color.bg_card)

        val spinner = ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(56, 56)
            indeterminateTintList = ColorStateList.valueOf(gold)
        }

        val label = TextView(context).apply {
            text = message
            setTextColor(white)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.leftMargin = 36 }
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(64, 48, 64, 48)
            setBackgroundColor(cardBg)
            addView(spinner)
            addView(label)
        }

        // Wrap in outer card to get rounded corners
        val outerCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = context.getDrawable(R.drawable.bg_service_card)
            addView(container)
        }

        dialog = AlertDialog.Builder(context)
            .setView(outerCard)
            .setCancelable(false)
            .create()
            .also { dlg ->
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.show()
            }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
