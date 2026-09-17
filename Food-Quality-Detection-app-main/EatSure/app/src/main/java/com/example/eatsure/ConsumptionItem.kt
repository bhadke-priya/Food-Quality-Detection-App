package com.example.eatsure

import com.google.gson.annotations.SerializedName

data class ConsumptionItem(
    @SerializedName("productName")
    val productName: String = "",

    @SerializedName("barcode")
    val barcode: String = "",

    @SerializedName("nutrients")
    val nutrients: Map<String, Double> = emptyMap(),

    @SerializedName("frequency")
    val frequency: String = "",

    @SerializedName("servingSize")
    val servingSize: Float = 0f
)