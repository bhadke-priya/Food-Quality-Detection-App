package com.example.eatsure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val _scanHistory = MutableLiveData<List<ScanItem>>()
    val scanHistory: LiveData<List<ScanItem>> = _scanHistory
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadScanHistory()
    }

    private fun loadScanHistory() {
        val user = firebaseAuth.currentUser
        user?.let {
            database.getReference("users")
                .child(it.uid)
                .child("scans")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val scans = mutableListOf<ScanItem>()
                        for (scanSnapshot in snapshot.children) {
                            val productName = scanSnapshot.child("productName").getValue(String::class.java)
                            val ImageUrl = scanSnapshot.child("imageUrl").getValue(String::class.java) // Ensure this matches Firebase
                            val nutriScore = scanSnapshot.child("nutriScore").getValue(String::class.java)
                            val timestamp = scanSnapshot.child("timestamp").getValue(String::class.java)
                            val barcode = scanSnapshot.child("barcode").getValue(String::class.java)

                            android.util.Log.d("HistoryViewModel", "Scan: name=$productName, image=$ImageUrl, barcode=$barcode")
                            if (productName != null && timestamp != null && barcode != null) {
                                scans.add(ScanItem(productName, ImageUrl ?:"", nutriScore, timestamp, barcode))
                            } else {
                                android.util.Log.w("HistoryViewModel", "Skipping scan: name=$productName, timestamp=$timestamp, barcode=$barcode")
                            }
                        }

                        val uniqueScans = scans
                            .sortedByDescending { it.timestamp }
                            .distinctBy { it.barcode }
                            .sortedByDescending { it.timestamp }
                        _scanHistory.value = uniqueScans
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _error.value = error.message
                    }
                })
        } ?: run {
            _error.value = "User not logged in"
        }
    }
}