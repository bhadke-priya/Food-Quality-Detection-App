package com.example.eatsure

import com.google.gson.annotations.SerializedName

// Data class for Nutriments (nutrition facts)
data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energy_kcal_100g: Double? = null,

    @SerializedName("fat_100g")
    val fat_100g: Double? = null,

    @SerializedName("saturated-fat_100g")
    val saturated_fat_100g: Double? = null,

    @SerializedName("carbohydrates_100g")
    val carbohydrates_100g: Double? = null,

    @SerializedName("sugars_100g")
    val sugars_100g: Double? = null,

    @SerializedName("proteins_100g")
    val proteins_100g: Double? = null,

    @SerializedName("salt_100g")
    val salt_100g: Double? = null,

    @SerializedName("fiber_100g")
    val fiber_100g: Double? = null,

    @SerializedName("trans-fat_100g")
    val trans_fat_100g: Double? = null,

    @SerializedName("cholesterol_100g")
    val cholesterol_100g: Double? = null,

    @SerializedName("sodium_100g")
    val sodium_100g: Double? = null,

    @SerializedName("added_sugars_100g")
    val added_sugars_100g: Double? = null


)

// Data class for the Product
data class Product(
    @SerializedName("product_name")
    val product_name: String? = null,

    @SerializedName("ingredients_text_debug")
    val ingredients_text: String? = null,

    @SerializedName("ingredients_text_with_allergens_en")
    val ingredients_text_with_allergens: String? = null,

    @SerializedName("nutriments")
    val nutriments: Nutriments? = null,

    @SerializedName("nutriscore_grade")
    val nutriscore_grade: String? = null,

    @SerializedName("nova_group_debug")
    val nova_group: String? = null, // Changed from Int? to String?

    @SerializedName("labels")
    val labels: String? = null,

    @SerializedName("image_url")
    val image_url: String? = null,

    val serving_size: String?,

    @SerializedName("ingredients")
    val ingredients: List<Ingredient>? = null
)

data class Ingredient(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("percent_estimate") val percentEstimate: Double,
    @SerializedName("percent_min") val percentMin: Double,
    @SerializedName("percent_max") val percentMax: Double,
    @SerializedName("is_in_taxonomy") val isInTaxonomy: Int,
    @SerializedName("vegan") val vegan: String? = null,
    @SerializedName("vegetarian") val vegetarian: String? = null,
    @SerializedName("ingredients") val ingredients: List<Ingredient>? = null,
    @SerializedName("ciqual_food_code") val ciqualFoodCode: String? = null
)

// Data class for the API response (assuming a wrapper object)
data class ProductResponse(
    @SerializedName("product")
    val product: Product? = null
)