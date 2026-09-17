package com.example.eatsure

import com.google.firebase.database.PropertyName

data class HealthReport(
    @PropertyName("nutrientSummary")
    val nutrientSummary: Map<String, Double> = emptyMap(),

    @PropertyName("risks")
    val risks: List<String> = emptyList(),

    @PropertyName("recommendations")
    val recommendations: List<String> = emptyList(),

    @PropertyName("overallScore")
    val overallScore: Int = 0
)

//package com.example.eatsure
//
//import com.google.gson.annotations.SerializedName
//
//data class HealthReport(
//    @SerializedName("nutrientSummary")
//    val nutrientSummary: Map<String, Double> = emptyMap(),
//
//    @SerializedName("risks")
//    val risks: List<String> = emptyList(),
//
//    @SerializedName("recommendations")
//    val recommendations: List<String> = emptyList(),
//
//    @SerializedName("overallScore")
//    val overallScore: Int = 0
//)

//data class HealthReport1(
//    val nutrientSummary: Map<String, Double>,  // Your nutrient grades
//    val risks: List<String>,                   // Health warnings
//    val recommendations: List<String>,         // What you should do
//    val overallScore: Int                      // Your overall health score (1-100)
//)

