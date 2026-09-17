package com.example.eatsure


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user
    private val _fullName = MutableLiveData<String?>()
    val fullName: LiveData<String?> = _fullName
    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    init {
        // Load current user and full name
        _user.value = firebaseAuth.currentUser
        loadFullName()
    }

    private fun loadFullName() {
        val user = firebaseAuth.currentUser
        user?.let {
            database.getReference("users")
                .child(it.uid)
                .child("fullName")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _fullName.value = snapshot.getValue(String::class.java)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _fullName.value = null
                    }
                })
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _logoutSuccess.value = true
    }
}