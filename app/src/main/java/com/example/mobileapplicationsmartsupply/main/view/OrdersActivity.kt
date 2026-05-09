package com.example.mobileapplicationsmartsupply.main.view

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.example.mobileapplicationsmartsupply.BaseActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.data.model.Order
import com.example.mobileapplicationsmartsupply.main.model.OrderRepository
import com.google.firebase.firestore.ListenerRegistration

class OrdersActivity : BaseActivity() {

    private lateinit var adapter: OrderAdapter
    private val repository = OrderRepository()
    private var allOrders: List<Order> = emptyList()
    private var ordersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        BottomNavHelper.setup(this, BottomNavHelper.TAB_REQUEST)

        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("order_id", order.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        setupSearch()
    }

    override fun onStart() {
        super.onStart()
        ordersListener = repository.listenToOrders { orders ->
            allOrders = orders
            updateStats(orders)
            adapter.updateOrders(orders)
        }
    }

    override fun onStop() {
        super.onStop()
        ordersListener?.remove()
    }

    private fun updateStats(orders: List<Order>) {
        findViewById<TextView>(R.id.tvPendingCount).text =
            orders.count { it.status == "Pending" }.toString()
        findViewById<TextView>(R.id.tvCompletedCount).text =
            orders.count { it.status == "Completed" }.toString()
        findViewById<TextView>(R.id.tvRejectedCount).text =
            orders.count { it.status == "Rejected" }.toString()
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearch).addTextChangedListener { text ->
            val query = text.toString().trim().lowercase()
            val filtered = if (query.isEmpty()) allOrders
            else allOrders.filter {
                it.title.lowercase().contains(query) ||
                it.category.lowercase().contains(query) ||
                it.subService.lowercase().contains(query) ||
                it.description.lowercase().contains(query) ||
                it.requestId.lowercase().contains(query)
            }
            adapter.updateOrders(filtered)
        }
    }
}
