package com.example.eatsure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale
import kotlinx.coroutines.tasks.await

class ReportViewModel : ViewModel() {

    private val TAG = "ReportViewModel"
    private val geminiApiService = GeminiApiService()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _report = MutableLiveData<HealthReport?>()
    val report: LiveData<HealthReport?> = _report

    private val _scanHistory = MutableLiveData<List<ScanItem>>()
    val scanHistory: LiveData<List<ScanItem>> = _scanHistory

    // Open Food Facts API client
    private val openFoodFactsApi = ApiClient.api

//    private val _savedReports = MutableLiveData<List<SavedHealthReport>>()
//    val savedReports: LiveData<List<SavedHealthReport>> = _savedReports

    init {
        loadScanHistory()
//        loadSavedReports()
    }

    private fun loadScanHistory() {
        val user = auth.currentUser
        if (user == null) {
            _error.value = "User not logged in"
            Log.e("ReportViewModel", "User not logged in")
            return
        }

        database.getReference("users")
            .child(user.uid)
            .child("scans") // Or "health_reports" if that's the correct path
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ReportViewModel", "Raw snapshot: ${snapshot.value}")
                    val scans = mutableListOf<ScanItem>()
                    for (scanSnapshot in snapshot.children) {
                        try {
                            val scan = scanSnapshot.getValue(ScanItem::class.java)
                            if (scan != null && scan.productName.isNotEmpty() && scan.timestamp.isNotEmpty() && scan.barcode.isNotEmpty()) {
                                scans.add(scan)
                                Log.d("ReportViewModel", "Parsed scan: $scan")
                            } else {
                                Log.w("ReportViewModel", "Skipping invalid scan: $scan")
                            }
                        } catch (e: Exception) {
                            Log.e("ReportViewModel", "Error parsing scan: ${e.message}", e)
                        }
                    }
                    val uniqueScans = scans
                        .sortedByDescending { it.timestamp }
                        .distinctBy { it.barcode }
                        .sortedByDescending { it.timestamp }
                    _scanHistory.postValue(uniqueScans)
                    Log.d("ReportViewModel", "Scans loaded: ${uniqueScans.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                    Log.e("ReportViewModel", "Firebase error: ${error.message}")
                }
            })
    }




    fun generateReport(
        age: Int,
        height: Float,
        weight: Float,
        gender: String,
        activityLevel: String,
        consumptionFrequencies: Map<String, Pair<String, Pair<String, Float>>>
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Validate inputs
                if (scanHistory.value.isNullOrEmpty()) {
                    Log.w(TAG, "No scan history available")
                    _error.value = "No scan history available. Please scan a product first."
                    _loading.value = false
                    return@launch
                }

                if (consumptionFrequencies.isEmpty()) {
                    Log.w(TAG, "No consumption frequencies provided")
                    _error.value = "Please specify consumption frequency for products"
                    _loading.value = false
                    return@launch
                }

                // Create user demographics
                val demographics = UserDemographics(
                    age = age,
                    gender = gender,
                    height = height,
                    weight = weight,
                    activityLevel = activityLevel
                )

                // Define fields to fetch from Open Food Facts API
                val fields = "product_name,ingredients_text_debug,ingredients_text_with_allergens_en,nutriments,nutriscore_grade,labels,image_url,serving_size,ingredients"

                // Fetch nutrients for each barcode and create consumption items
                val consumptionItems = mutableListOf<ConsumptionItem>()
                scanHistory.value?.forEach { scanItem ->
                    val frequencyData = consumptionFrequencies[scanItem.barcode]
                    if (frequencyData != null) {
                        try {
                            Log.d(TAG, "Fetching nutrients for barcode ${scanItem.barcode}")
                            val productResponse = openFoodFactsApi.getProduct(scanItem.barcode, fields)
                            val nutriments = productResponse.product?.nutriments

                            if (nutriments != null) {
                                val nutrientsMap = mapOf(
                                    "energy_kcal_100g" to (nutriments.energy_kcal_100g ?: 0.0),
                                    "fat_100g" to (nutriments.fat_100g ?: 0.0),
                                    "saturated_fat_100g" to (nutriments.saturated_fat_100g ?: 0.0),
                                    "carbohydrates_100g" to (nutriments.carbohydrates_100g ?: 0.0),
                                    "sugars_100g" to (nutriments.sugars_100g ?: 0.0),
                                    "proteins_100g" to (nutriments.proteins_100g ?: 0.0),
                                    "salt_100g" to (nutriments.salt_100g ?: 0.0),
                                    "fiber_100g" to (nutriments.fiber_100g ?: 0.0),
                                    "trans_fat_100g" to (nutriments.trans_fat_100g ?: 0.0),
                                    "cholesterol_100g" to (nutriments.cholesterol_100g ?: 0.0),
                                    "sodium_100g" to (nutriments.sodium_100g ?: 0.0),
                                    "added_sugars_100g" to (nutriments.added_sugars_100g ?: 0.0)
                                )

                                Log.d(TAG, "Nutrients for ${scanItem.barcode}: $nutrientsMap")

                                if (nutrientsMap.isNotEmpty()) {
                                    consumptionItems.add(
                                        ConsumptionItem(
                                            productName = scanItem.productName,
                                            barcode = scanItem.barcode,
                                            nutrients = nutrientsMap,
                                            frequency = frequencyData.second.first,
                                            servingSize = frequencyData.second.second
                                        )
                                    )
                                }
                            } else {
                                Log.w(TAG, "No nutriments found for barcode ${scanItem.barcode}")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch nutrients for barcode ${scanItem.barcode}: ${e.message}", e)
                        }
                    }
                }

                if (consumptionItems.isEmpty()) {
                    Log.w(TAG, "No valid consumption data available")
                    _error.value = "No valid consumption data available. Please ensure product data is available."
                    _loading.value = false
                    return@launch
                }

                Log.d(TAG, "Generated ${consumptionItems.size} consumption items: $consumptionItems")

                // Generate report using Gemini
                val healthReport = geminiApiService.generateHealthReport(demographics, consumptionItems)

                Log.d(TAG, "Health Report Generated at ${System.currentTimeMillis()}")
                Log.d(TAG, "Nutrient Summary: ${healthReport.nutrientSummary}")
                Log.d(TAG, "Risks: ${healthReport.risks}")
                Log.d(TAG, "Recommendations: ${healthReport.recommendations}")
                Log.d(TAG, "Overall Score: ${healthReport.overallScore}")

//                 Save the report to Firebase
                val savedReport = saveReportToFirebase(healthReport, demographics, consumptionItems)

                if (savedReport != null) {
                    _report.value = healthReport
                    Log.d(TAG, "Health report generated and saved successfully")
                } else {
                    _error.value = "Failed to save report to database"
                }

                _loading.value = false


            } catch (e: Exception) {
                    Log.e(TAG, "Failed to generate health report: ${e.message}", e)
                    _error.value = "Failed to generate health report: ${e.message}"
                } finally {
                    _loading.value = false
                }
        }
    }

    suspend fun saveReportToFirebase(
        healthReport: HealthReport,
        demographics: UserDemographics,
        consumptionItems: List<ConsumptionItem>
    ) {
        try {
            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user")
                return
            }

            val userId = currentUser.uid
            val timestamp = System.currentTimeMillis()
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormatter.format(Date(timestamp))

            val savedReport = SavedHealthReport(
                reportId = "", // Firebase will assign the ID
                healthReport = healthReport,
                demographics = demographics,
                consumptionItems = consumptionItems,
                timestamp = timestamp,
                formattedDate = formattedDate
            )

            // Save to Firebase
            val reportRef = database.reference
                .child("users")
                .child(userId)
                .child("health_reports")
                .push()

            reportRef.setValue(savedReport).await()
            Log.d(TAG, "Health report saved successfully with ID: ${reportRef.key}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving health report: ${e.message}", e)
        }
    }

//    private suspend fun saveReportToFirebase(
//        healthReport: HealthReport,
//        demographics: UserDemographics,
//        consumptionItems: List<ConsumptionItem>
//    ): SavedHealthReport?
//    {
//        return try {
//            val currentUser = auth.currentUser
//            if (currentUser == null) {
//                Log.e(TAG, "No authenticated user")
//                return null
//            }
//
//            val userId = currentUser.uid
//            val timestamp = System.currentTimeMillis()
//            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//            val formattedDate = dateFormatter.format(Date(timestamp))
//
//            val savedReport = SavedHealthReport(
//                reportId = "", // Will be set after saving
//                healthReport = healthReport,
//                demographics = demographics,
//                consumptionItems = consumptionItems,
//                timestamp = timestamp,
//                formattedDate = formattedDate
//            )
//
//            // Save to Firebase
//            val reportRef = database.reference
//                .child("users")
//                .child(userId)
//                .child("health_reports")
//                .push()
//
//            reportRef.setValue(savedReport).addOnSuccessListener {
//                Log.d(TAG, "Health report saved successfully with ID: ${reportRef.key}")
//            }.addOnFailureListener { exception ->
//                Log.e(TAG, "Failed to save health report: ${exception.message}")
//            }
//
//            savedReport.copy(reportId = reportRef.key ?: "")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saving health report: ${e.message}", e)
//            null
//        }
//    }

//    fun deleteReport(reportId: String) {
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            _error.value = "Please log in to delete reports"
//            return
//        }
//
//        val userId = currentUser.uid
//        database.reference
//            .child("users")
//            .child(userId)
//            .child("health_reports")
//            .child(reportId)
//            .removeValue()
//            .addOnSuccessListener {
//                Log.d(TAG, "Report deleted successfully")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Failed to delete report: ${exception.message}")
//                _error.value = "Failed to delete report: ${exception.message}"
//            }
//    }

    fun clearReport() {
        _report.value = null
        _error.value = ""
    }
}