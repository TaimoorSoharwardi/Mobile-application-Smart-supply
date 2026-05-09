package com.example.mobileapplicationsmartsupply.main.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobileapplicationsmartsupply.BaseActivity
import androidx.core.content.ContextCompat
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.data.model.Order
import com.example.mobileapplicationsmartsupply.main.model.OrderRepository

class OrderDetailActivity : BaseActivity() {

    private val repository = OrderRepository()
    private lateinit var order: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val orderId = intent.getStringExtra("order_id") ?: run { finish(); return }

        LoadingDialog.show(this, "Loading request...")

        repository.getOrderById(orderId,
            onSuccess = { fetchedOrder ->
                LoadingDialog.dismiss()
                if (fetchedOrder == null) {
                    Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@getOrderById
                }
                order = fetchedOrder
                bindData()
                setupActions()
            },
            onError = { error ->
                LoadingDialog.dismiss()
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                finish()
            }
        )

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun bindData() {
        findViewById<TextView>(R.id.tvTitle).text = order.title
        findViewById<TextView>(R.id.tvCategory).text = order.category
        findViewById<TextView>(R.id.tvRequestId).text = order.requestId
        findViewById<TextView>(R.id.tvDescription).text = order.description
        findViewById<TextView>(R.id.tvQuantity).text = "${order.quantity} persons"
        findViewById<TextView>(R.id.tvDate).text = order.date
        findViewById<TextView>(R.id.tvAddress).text = order.address.ifEmpty { "—" }
        findViewById<TextView>(R.id.tvCurrentPrice).text = order.price

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = order.status
        when (order.status) {
            "Pending" -> {
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gold))
                tvStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_pending)
            }
            "Completed" -> {
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.active_green))
                tvStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_complete)
            }
            "Rejected" -> {
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_rejected))
                tvStatus.background = ContextCompat.getDrawable(this, R.drawable.bg_status_rejected)
            }
        }

        val etPrice = findViewById<EditText>(R.id.etPrice)
        if (order.price != "PKR —" && order.price.isNotEmpty()) {
            etPrice.setText(order.price.removePrefix("PKR ").trim())
        }
    }

    private fun setupActions() {
        val btnComplete = findViewById<Button>(R.id.btnMarkComplete)
        val btnReject = findViewById<Button>(R.id.btnReject)

        when (order.status) {
            "Completed" -> setButtonDisabled(btnComplete, "✓ Completed")
            "Rejected" -> setButtonDisabled(btnReject, "✗ Rejected")
        }

        btnComplete.setOnClickListener {
            if (order.status == "Completed") {
                Toast.makeText(this, "Already completed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val price = findViewById<EditText>(R.id.etPrice).text.toString().trim()
            if (price.isEmpty()) {
                Toast.makeText(this, "Please set an amount before marking complete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateStatus("Completed", "PKR $price", btnComplete, btnReject)
        }

        btnReject.setOnClickListener {
            if (order.status == "Rejected") {
                Toast.makeText(this, "Already rejected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val price = findViewById<EditText>(R.id.etPrice).text.toString().trim()
            val finalPrice = if (price.isNotEmpty()) "PKR $price" else order.price
            updateStatus("Rejected", finalPrice, btnComplete, btnReject)
        }
    }

    private fun updateStatus(status: String, price: String, btnComplete: Button, btnReject: Button) {
        btnComplete.isEnabled = false
        btnReject.isEnabled = false
        LoadingDialog.show(this, if (status == "Completed") "Completing..." else "Rejecting...")

        val updated = order.copy(status = status, price = price)
        repository.updateOrder(updated,
            onSuccess = {
                order = updated
                LoadingDialog.dismiss()
                Toast.makeText(this, "Request marked as $status", Toast.LENGTH_SHORT).show()
                bindData()
                when (status) {
                    "Completed" -> {
                        setButtonDisabled(btnComplete, "✓ Completed")
                        btnReject.isEnabled = true
                        btnReject.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_reject)
                        btnReject.alpha = 1f
                    }
                    "Rejected" -> {
                        setButtonDisabled(btnReject, "✗ Rejected")
                        btnComplete.isEnabled = true
                        btnComplete.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_complete)
                        btnComplete.alpha = 1f
                    }
                }
            },
            onError = { error ->
                LoadingDialog.dismiss()
                btnComplete.isEnabled = true
                btnReject.isEnabled = true
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setButtonDisabled(btn: Button, label: String) {
        btn.text = label
        btn.isEnabled = false
        btn.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_disabled)
        btn.alpha = 0.7f
    }
}
