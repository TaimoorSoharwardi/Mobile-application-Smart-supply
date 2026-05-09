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

    private val _stats = MutableLiveData<Triple<Int, Int, Int>>()
    val stats: LiveData<Triple<Int, Int, Int>> = _stats

    fun fetchHomeData() {
        repository.getAllOrders(
            onSuccess = { orders ->
                _recentOrders.value = orders.take(5)
                _stats.value = Triple(
                    orders.size,
                    orders.count { it.status == "Pending" },
                    orders.count { it.status == "Completed" }
                )
            },
            onError = { /* silently ignore on home */ }
        )
    }
}
