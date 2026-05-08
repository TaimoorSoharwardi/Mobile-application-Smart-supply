package com.example.mobileapplicationsmartsupply.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobileapplicationsmartsupply.data.model.Order
import com.example.mobileapplicationsmartsupply.main.model.OrderRepository

class MainViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _recentOrders = MutableLiveData<List<Order>>()
    val recentOrders: LiveData<List<Order>> = _recentOrders

    private val _stats = MutableLiveData<Triple<Int, Int, Int>>() // Total, Pending, Delivered
    val stats: LiveData<Triple<Int, Int, Int>> = _stats

    fun fetchHomeData() {
        val allOrders = repository.getSampleOrders()
        _recentOrders.value = allOrders.take(5)
        
        val total = allOrders.size
        val pending = allOrders.count { it.status == "Pending" }
        val delivered = allOrders.count { it.status == "Delivered" }
        _stats.value = Triple(total, pending, delivered)
    }
}
