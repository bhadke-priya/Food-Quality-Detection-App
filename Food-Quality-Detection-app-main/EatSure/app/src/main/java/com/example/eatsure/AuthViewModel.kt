package com.example.eatsure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference.child("users") // Realtime Database reference

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _signUpResult = MutableLiveData<AuthResult>()
    val signUpResult: LiveData<AuthResult> = _signUpResult

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun login(email: String, password: String) {
        _loginResult.value = AuthResult.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update last login time
                    updateLastLoginTime()
                    _loginResult.value = AuthResult.Success("Login successful")
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    _loginResult.value = AuthResult.Error(errorMessage)
                }
            }
    }

    fun signUp(fullName: String, email: String, password: String) {
        _signUpResult.value = AuthResult.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Save user data to Realtime Database
                    saveUserToRealtimeDatabase(fullName, email)
                } else {
                    val errorMessage = task.exception?.message ?: "Sign up failed"
                    _signUpResult.value = AuthResult.Error(errorMessage)
                }
            }
    }

    private fun saveUserToRealtimeDatabase(fullName: String, email: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _signUpResult.value = AuthResult.Error("User ID not found")
            return
        }

        // Create user data map
        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "createdAt" to Date().time, // Store timestamp for Realtime Database
            "lastLoginAt" to Date().time,
            "isActive" to true
        )

        // Save to Realtime Database under users/{uid}
        database.child(userId).setValue(userData)
            .addOnSuccessListener {
                _signUpResult.value = AuthResult.Success("Account created successfully")
            }
            .addOnFailureListener { exception ->
                _signUpResult.value = AuthResult.Error("Account created but profile setup failed: ${exception.message}")
            }
    }

    private fun updateLastLoginTime() {
        val userId = auth.currentUser?.uid ?: return

        // Update lastLoginAt in Realtime Database
        database.child(userId).child("lastLoginAt").setValue(Date().time)
            .addOnFailureListener {
                // Silent fail - login was successful, this is just metadata
            }
    }

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Handle in UI if needed
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}