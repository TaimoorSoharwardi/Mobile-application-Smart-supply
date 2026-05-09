package com.example.mobileapplicationsmartsupply.main.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.data.model.Order

class OrderAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOrderTitle)
        val tvSubService: TextView = view.findViewById(R.id.tvSubService)
        val tvDescription: TextView = view.findViewById(R.id.tvOrderDescription)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvPrice: TextView = view.findViewById(R.id.tvOrderPrice)
        val tvId: TextView = view.findViewById(R.id.tvOrderId)
        val tvDate: TextView = view.findViewById(R.id.tvOrderDate)
        val card: LinearLayout = view.findViewById(R.id.orderCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val ctx = holder.itemView.context

        holder.tvTitle.text = order.title
        if (order.subService.isNotEmpty()) {
            holder.tvSubService.text = order.subService
            holder.tvSubService.visibility = View.VISIBLE
        } else {
            holder.tvSubService.visibility = View.GONE
        }
        holder.tvDescription.text = order.description
        holder.tvPrice.text = order.price
        holder.tvId.text = "ID ${order.requestId}"
        holder.tvDate.text = order.date
        holder.tvStatus.text = order.status

        when (order.status) {
            "Pending" -> {
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.gold))
                holder.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                holder.card.background = ContextCompat.getDrawable(ctx, R.drawable.bg_card_gold_border)
            }
            "Completed" -> {
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.active_green))
                holder.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_complete)
                holder.card.background = ContextCompat.getDrawable(ctx, R.drawable.bg_card_green_border)
            }
            "Rejected" -> {
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_rejected))
                holder.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_rejected)
                holder.card.background = ContextCompat.getDrawable(ctx, R.drawable.bg_card_red_border)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
