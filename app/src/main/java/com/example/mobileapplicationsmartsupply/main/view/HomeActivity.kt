package com.example.mobileapplicationsmartsupply.main.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.auth.model.AuthRepository
import com.example.mobileapplicationsmartsupply.main.model.OrderRepository
import com.google.firebase.firestore.ListenerRegistration

class HomeActivity : BaseActivity() {

    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private lateinit var recentAdapter: OrderAdapter
    private var homeListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        BottomNavHelper.setup(this, BottomNavHelper.TAB_HOME)
        loadUserName()
        setupServiceButtons()
        setupRecentOrdersRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        homeListener = orderRepository.listenToOrders { orders ->
            // Update the 3 stat counters
            findViewById<TextView>(R.id.tvHomePending).text =
                orders.count { it.status == "Pending" }.toString()
            findViewById<TextView>(R.id.tvHomeCompleted).text =
                orders.count { it.status == "Completed" }.toString()
            findViewById<TextView>(R.id.tvHomeRejected).text =
                orders.count { it.status == "Rejected" }.toString()

            // Show/hide recent orders list (max 3 items)
            val recent = orders.take(3)
            val rvRecent = findViewById<RecyclerView>(R.id.rvRecentOrders)
            val tvEmpty = findViewById<TextView>(R.id.tvNoRecentRequests)
            if (recent.isEmpty()) {
                rvRecent.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            } else {
                rvRecent.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                recentAdapter.updateOrders(recent)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        homeListener?.remove()
    }

    private fun setupRecentOrdersRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvRecentOrders)
        rv.layoutManager = LinearLayoutManager(this)
        rv.isNestedScrollingEnabled = false
        recentAdapter = OrderAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("order_id", order.id)
            startActivity(intent)
        }
        rv.adapter = recentAdapter

        findViewById<TextView>(R.id.tvViewAllRequests).setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun setupServiceButtons() {
        findViewById<LinearLayout>(R.id.btnRequestNew).setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java))
        }

        mapOf(
            R.id.cardKitchen to "Kitchen",
            R.id.cardStationary to "Stationary",
            R.id.cardFurniture to "Furniture",
            R.id.cardEvents to "Events"
        ).forEach { (viewId, category) ->
            findViewById<LinearLayout>(viewId).setOnClickListener {
                startActivity(
                    Intent(this, NewRequestActivity::class.java)
                        .putExtra("category", category)
                )
            }
        }
    }

    private fun loadUserName() {
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        val email = authRepository.getCurrentUserEmail()
        if (email.isNotEmpty()) {
            tvUserName.text = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        authRepository.getCurrentUser(
            onSuccess = { user ->
                val name = "${user.firstName} ${user.lastName}".trim()
                tvUserName.text = name.ifEmpty {
                    email.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
            },
            onError = {}
        )
    }
}
