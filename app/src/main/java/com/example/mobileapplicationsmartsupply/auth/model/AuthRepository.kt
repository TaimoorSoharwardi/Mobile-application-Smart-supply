package com.example.mobileapplicationsmartsupply.auth.model

import com.example.mobileapplicationsmartsupply.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun isLoggedIn() = auth.currentUser != null

    fun getCurrentUserId() = auth.currentUser?.uid ?: ""

    fun getCurrentUserEmail() = auth.currentUser?.email ?: ""

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = hashMapOf(
                    "uid" to uid,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "phone" to phone,
                    "location" to "",
                    "company" to ""
                )
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Failed to save profile") }
            }
            .addOnFailureListener { onError(it.message ?: "Registration failed") }
    }

    fun getCurrentUser(onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) { onError("Not logged in"); return }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = User(
                    uid = uid,
                    firstName = doc.getString("firstName") ?: "",
                    lastName = doc.getString("lastName") ?: "",
                    email = doc.getString("email") ?: getCurrentUserEmail(),
                    phone = doc.getString("phone") ?: "",
                    location = doc.getString("location") ?: "",
                    company = doc.getString("company") ?: ""
                )
                onSuccess(user)
            }
            .addOnFailureListener { onError(it.message ?: "Failed to load profile") }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phone: String,
        location: String,
        company: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) { onError("Not logged in"); return }
        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "location" to location,
            "company" to company
        )
        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to update profile") }
    }

    fun logout() = auth.signOut()
}
