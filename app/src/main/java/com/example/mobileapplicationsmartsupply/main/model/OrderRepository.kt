package com.example.mobileapplicationsmartsupply.main.model

import com.example.mobileapplicationsmartsupply.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("orders")

    private fun currentUserId() = auth.currentUser?.uid ?: ""

    fun getAllOrders(onSuccess: (List<Order>) -> Unit, onError: (String) -> Unit) {
        collection
            .whereEqualTo("userId", currentUserId())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val orders = result.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                }
                onSuccess(orders)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load orders") }
    }

    fun listenToOrders(onUpdate: (List<Order>) -> Unit): ListenerRegistration {
        return collection
            .whereEqualTo("userId", currentUserId())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                    onUpdate(orders)
                }
            }
    }

    fun addOrder(order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val data = hashMapOf(
            "userId" to currentUserId(),
            "requestId" to order.requestId,
            "title" to order.title,
            "category" to order.category,
            "subService" to order.subService,
            "description" to order.description,
            "quantity" to order.quantity,
            "address" to order.address,
            "date" to order.date,
            "price" to order.price,
            "status" to order.status,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        collection.add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to submit request") }
    }

    fun updateOrder(order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (order.id.isEmpty()) { onError("Invalid order ID"); return }
        val updates = mapOf(
            "status" to order.status,
            "price" to order.price
        )
        collection.document(order.id).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update request") }
    }

    fun getOrderById(id: String, onSuccess: (Order?) -> Unit, onError: (String) -> Unit) {
        collection.document(id).get()
            .addOnSuccessListener { doc ->
                val order = doc.toObject(Order::class.java)?.copy(id = doc.id)
                onSuccess(order)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load request") }
    }

    fun getOrderCount(onSuccess: (Int) -> Unit) {
        collection.whereEqualTo("userId", currentUserId()).get()
            .addOnSuccessListener { onSuccess(it.size()) }
            .addOnFailureListener { onSuccess(0) }
    }
}
