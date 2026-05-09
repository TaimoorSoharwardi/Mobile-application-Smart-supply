package com.example.mobileapplicationsmartsupply.main.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.mobileapplicationsmartsupply.BaseActivity
import com.example.mobileapplicationsmartsupply.R
import com.example.mobileapplicationsmartsupply.data.model.Order
import com.example.mobileapplicationsmartsupply.main.model.OrderRepository
import com.google.firebase.auth.FirebaseAuth

class NewRequestActivity : BaseActivity() {

    private var selectedCategory = ""
    private var selectedSubService = ""
    private val repository = OrderRepository()
    private lateinit var btnSubmit: Button
    private lateinit var tvSubServiceSelector: TextView
    private lateinit var layoutSubService: LinearLayout

    private val subServices = mapOf(
        "Kitchen" to listOf("Daily Catering", "Tea & Beverages", "Snacks & Refreshments", "Pantry Restocking", "Kitchen Equipment"),
        "Stationary" to listOf("Office Supplies", "Printing & Photocopying", "Filing & Storage", "Printer Cartridges", "Desk Accessories"),
        "Furniture" to listOf("Office Chairs", "Desks & Tables", "Storage Cabinets", "Reception Furniture", "Partitions & Dividers"),
        "Events" to listOf("Conference Setup", "Corporate Dinner", "Training Workshop", "Product Launch", "Team Building")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        btnSubmit = findViewById(R.id.btnSubmit)
        tvSubServiceSelector = findViewById(R.id.tvSubServiceSelector)
        layoutSubService = findViewById(R.id.layoutSubService)

        BottomNavHelper.setup(this, BottomNavHelper.TAB_REQUEST)

        selectedCategory = intent.getStringExtra("category") ?: ""
        if (selectedCategory.isNotEmpty()) {
            highlightCategory(selectedCategory)
            revealSubServiceSelector(selectedCategory)
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        mapOf(
            R.id.catKitchen to "Kitchen",
            R.id.catStationary to "Stationary",
            R.id.catFurniture to "Furniture",
            R.id.catEvents to "Events"
        ).forEach { (viewId, name) ->
            findViewById<LinearLayout>(viewId).setOnClickListener {
                if (selectedCategory != name) selectedSubService = ""
                selectedCategory = name
                highlightCategory(name)
                revealSubServiceSelector(name)
            }
        }

        tvSubServiceSelector.setOnClickListener { openSubServicePicker() }
        btnSubmit.setOnClickListener { submitRequest() }

        // Pre-populate email from Firebase Auth so the user doesn't have to type it
        val authEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        if (authEmail.isNotEmpty()) {
            findViewById<EditText>(R.id.etEmail).setText(authEmail)
        }
    }

    private fun revealSubServiceSelector(category: String) {
        layoutSubService.visibility = View.VISIBLE
        if (selectedSubService.isEmpty()) {
            tvSubServiceSelector.text = "Select service type ▼"
            tvSubServiceSelector.setTextColor(ContextCompat.getColor(this, R.color.hint_gray))
        }
    }

    private fun openSubServicePicker() {
        val options = subServices[selectedCategory] ?: return
        val currentIndex = options.indexOf(selectedSubService).takeIf { it >= 0 } ?: -1
        AlertDialog.Builder(this)
            .setTitle("Select Service Type")
            .setSingleChoiceItems(options.toTypedArray(), currentIndex) { dialog, which ->
                selectedSubService = options[which]
                tvSubServiceSelector.text = selectedSubService
                tvSubServiceSelector.setTextColor(ContextCompat.getColor(this, R.color.white))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitRequest() {
        val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()
        val phone = findViewById<EditText>(R.id.etPhone).text.toString().trim()
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val location = findViewById<EditText>(R.id.etLocation).text.toString().trim()
        val quantityStr = findViewById<EditText>(R.id.etQuantity).text.toString().trim()
        val date = findViewById<EditText>(R.id.etDate).text.toString().trim()

        when {
            selectedCategory.isEmpty() -> toast("Please select a service category")
            selectedSubService.isEmpty() -> toast("Please select a service type")
            fullName.isEmpty() -> toast("Please enter your full name")
            phone.isEmpty() -> toast("Please enter your phone number")
            email.isEmpty() -> toast("Please enter your email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                toast("Enter a valid email — e.g. name@company.com")
            location.isEmpty() -> toast("Please enter your location")
            quantityStr.isEmpty() -> toast("Please enter the quantity")
            date.isEmpty() -> toast("Please enter the required date (DD / MM / YYYY)")
            !isValidDate(date) -> toast("Enter date as DD / MM / YYYY — e.g. 25 / 06 / 2026")
            else -> saveRequest(fullName, phone, email, quantityStr.toInt(), location, date)
        }
    }

    private fun isValidDate(date: String): Boolean {
        val cleaned = date.replace(" ", "").replace("/", "").replace("-", "")
        if (cleaned.length != 8) return false
        val day = cleaned.substring(0, 2).toIntOrNull() ?: return false
        val month = cleaned.substring(2, 4).toIntOrNull() ?: return false
        val year = cleaned.substring(4, 8).toIntOrNull() ?: return false
        return day in 1..31 && month in 1..12 && year >= 2024
    }

    private fun saveRequest(name: String, phone: String, email: String, quantity: Int, location: String, date: String) {
        btnSubmit.isEnabled = false
        btnSubmit.alpha = 0.6f
        LoadingDialog.show(this, "Submitting your request...")

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val requestId = "REQ-${System.currentTimeMillis() % 100000}"

        val order = Order(
            userId = uid,
            requestId = requestId,
            title = "$selectedCategory Services",
            category = selectedCategory,
            subService = selectedSubService,
            description = "Request by $name — $phone — $email",
            quantity = quantity,
            address = location,
            date = date,
            price = "PKR —",
            status = "Pending"
        )

        repository.addOrder(order,
            onSuccess = {
                LoadingDialog.dismiss()
                toast("Request submitted successfully!")
                finish()
            },
            onError = { error ->
                LoadingDialog.dismiss()
                btnSubmit.isEnabled = true
                btnSubmit.alpha = 1f
                toast(error)
            }
        )
    }

    private fun highlightCategory(category: String) {
        mapOf(
            "Kitchen" to R.id.catKitchen,
            "Stationary" to R.id.catStationary,
            "Furniture" to R.id.catFurniture,
            "Events" to R.id.catEvents
        ).forEach { (name, viewId) ->
            findViewById<LinearLayout>(viewId).background = ContextCompat.getDrawable(
                this,
                if (name == category) R.drawable.bg_card_gold_border else R.drawable.bg_category_icon
            )
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
