package com.example.eatsure

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class ScanItem(
    @PropertyName("productName") val productName: String = "",
    @PropertyName("imageUrl") val imageUrl: String? = null,
    @PropertyName("nutriScore") val nutriScore: String? = null,
    @PropertyName("timestamp") val timestamp: String = "",
    @PropertyName("barcode") val barcode: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", null, null, "", "")
}

//package com.example.eatsure
//
//data class ScanItem(
//    val productName: String,
//    val imageUrl: String?,
//    val nutriScore: String?,
//    val timestamp: String,
//    val barcode: String,
//)