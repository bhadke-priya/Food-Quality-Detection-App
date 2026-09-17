package com.example.eatsure

import com.google.gson.annotations.SerializedName

data class SavedHealthReport(
    @SerializedName("reportId")
    val reportId: String = "",

    @SerializedName("healthReport")
    val healthReport: HealthReport = HealthReport(),

    @SerializedName("demographics")
    val demographics: UserDemographics = UserDemographics(
        age = 0,
        gender = "",
        height = 0f,
        weight = 0f,
        activityLevel = ""
    ),

    @SerializedName("consumptionItems")
    val consumptionItems: List<ConsumptionItem> = emptyList(),

    @SerializedName("timestamp")
    val timestamp: Long = 0L,

    @SerializedName("formattedDate")
    val formattedDate: String = ""
)

