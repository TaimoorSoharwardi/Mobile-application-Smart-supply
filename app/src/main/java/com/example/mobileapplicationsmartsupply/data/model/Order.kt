package com.example.mobileapplicationsmartsupply.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val requestId: String = "",
    val title: String = "",
    val category: String = "",
    val subService: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val address: String = "",
    val date: String = "",
    val price: String = "",
    val status: String = ""
)
