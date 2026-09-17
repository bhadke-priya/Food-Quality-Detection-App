package com.example.eatsure

import com.google.gson.annotations.SerializedName

data class UserDemographics(
    @SerializedName("age")
    val age: Int = 0,

    @SerializedName("gender")
    val gender: String = "",

    @SerializedName("height")
    val height: Float = 0f,

    @SerializedName("weight")
    val weight: Float = 0f,

    @SerializedName("activityLevel")
    val activityLevel: String = ""
)
