package com.example.eatsure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val apiService = ApiClient.api
    private val _scanSaved = MutableLiveData<Boolean>()
    val scanSaved: LiveData<Boolean> = _scanSaved
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun saveScan(barcode: String) {
        viewModelScope.launch {
            try {
                val fields = "product_name,image_url,nutriscore_grade"
                val response = apiService.getProduct(barcode, fields)
                if (response.product != null) {
                    val product = response.product
                    val scanData = mapOf(
                        "barcode" to barcode,
                        "productName" to (product.product_name ?: "Unknown Product"),
                        "imageUrl" to (product.image_url ?: "Image not loaded"),
                        "nutriScore" to (product.nutriscore_grade ?: "Not known"),
                        "timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' '", Locale.US).format(Date())
                    )
                    saveToFirebase(scanData)
                } else {
                    _error.value = "Product not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch product: ${e.message}"
            }
        }
    }

    private fun saveToFirebase(scanData: Map<String, String>) {
        val user = firebaseAuth.currentUser
        user?.let {
            database.getReference("users")
                .child(it.uid)
                .child("scans")
                .push()
                .setValue(scanData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("HomeViewModel", "Saved scan: $scanData")
                        _scanSaved.value = true
                    } else {
                        android.util.Log.e("HomeViewModel", "Save failed", task.exception)
                        _error.value = task.exception?.message ?: "Failed to save scan"

                    }
                }
        } ?: run {
            _error.value = "User not logged in"
        }
    }
}