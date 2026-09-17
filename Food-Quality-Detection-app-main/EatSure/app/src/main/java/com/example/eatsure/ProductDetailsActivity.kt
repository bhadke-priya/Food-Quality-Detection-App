package com.example.eatsure

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.eatsure.databinding.ActivityProductDetailsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

// Data class for nutrient limits
data class NutrientLimit(
    val name: String,
    val weeklyLimitGrams: Double,
    val monthlyLimitGrams: Double
)

// Data class for consumption limits
data class ConsumptionLimit(
    val nutrient: String,
    val weeklyServings: Double,
    val monthlyServings: Double
)

// Static nutrient limits based on WHO/DGA (2000-calorie diet)
val nutrientLimits = listOf(
    NutrientLimit("Sugar", 350.0, 1400.0), // WHO: <50g/day
    NutrientLimit("Sodium", 14.0, 56.0),   // WHO: <2g/day
    NutrientLimit("Saturated Fat", 154.0, 616.0) // DGA: <22g/day
)

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get barcode from Intent
        val barcode = intent.getStringExtra("barcode")

        if (barcode != null) {
            fetchProductData(barcode)
        } else {
            binding.productName.text = "No barcode found."
            binding.consumptionLimitsTable.isVisible = false
        }
    }

    private fun fetchProductData(barcode: String) {
        if (barcode.isEmpty()) {
            binding.productName.text = "No barcode available."
            binding.consumptionLimitsTable.isVisible = false
            return
        }

        val fields = "product_name,ingredients_text-debug,ingredients_text_with_allergens_en,nutriments,nutriscore_grade,labels,image_url,serving_size,ingredients"

        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getProduct(barcode, fields)
                val product = response.product

                // Update UI with product data
                val productNameText = product?.product_name ?: "Unknown Product"
                binding.productName.text = productNameText

                // Load image with Glide
                val imageUrl = product?.image_url
                imageUrl?.let { url ->
                    Glide.with(this@ProductDetailsActivity)
                        .load(url)
                        .placeholder(R.drawable.placeholder_image)
                        .into(binding.productImage)
                }

                // Display Nutri-Score and set badge color
                val nutriScore = product?.nutriscore_grade?.uppercase() ?: "Not available"
                binding.nutriScoreBadge.text = nutriScore
                setNutriScoreBadgeColor(nutriScore)

                // Display ingredients in tabular format
                val ingredients = product?.ingredients_text_with_allergens ?: product?.ingredients_text
                if (ingredients != null) {
                    populateIngredientsTable(ingredients)
                } else {
                    addIngredientRow("Not available")
                }

                // Display nutrition facts in tabular format
                product?.nutriments?.let { nutriments ->
                    binding.caloriesValue.text = "${nutriments.energy_kcal_100g?.toString() ?: "N/A"} kcal"
                    binding.fatValue.text = "${nutriments.fat_100g?.toString() ?: "N/A"} g"
                    binding.saturatedFatValue.text = "${nutriments.saturated_fat_100g?.toString() ?: "N/A"} g"
                    binding.carbohydratesValue.text = "${nutriments.carbohydrates_100g?.toString() ?: "N/A"} g"
                    binding.sugarsValue.text = "${nutriments.sugars_100g?.toString() ?: "N/A"} g"
                    binding.proteinsValue.text = "${nutriments.proteins_100g?.toString() ?: "N/A"} g"
                    binding.saltValue.text = "${nutriments.salt_100g?.toString() ?: "N/A"} g"

                    // Populate emulsifiers table
                    val emulsifiers = extractEmulsifiers(product.ingredients)
                    populateEmulsifiersTable(emulsifiers)

                    // Calculate and display consumption limits
                    val limits = calculateConsumptionLimits(nutriments, product.serving_size)
                    populateConsumptionLimitsTable(limits)
                } ?: run {
                    binding.consumptionLimitsTable.isVisible = false
                }

                // Display Labels as tags
                val labels = product?.labels?.split(",")?.map { it.trim() } ?: listOf("Not available")
                populateLabels(labels)

            } catch (e: Exception) {
                binding.productName.text = "Error fetching product data: ${e.message}"
                binding.consumptionLimitsTable.isVisible = false
            }
        }
    }

    private fun extractEmulsifiers(ingredients: List<Ingredient>?): List<Pair<String, Double>> {
        val emulsifiers = mutableListOf<Pair<String, Double>>()
        ingredients?.forEach { ingredient ->
            if (ingredient.id == "en:emulsifier") {
                emulsifiers.add(Pair(ingredient.text, ingredient.percentEstimate))
                ingredient.ingredients?.forEach { subIngredient ->
                    emulsifiers.add(Pair(subIngredient.text, subIngredient.percentEstimate))
                    subIngredient.ingredients?.forEach { subSubIngredient ->
                        emulsifiers.add(Pair(subSubIngredient.text, subSubIngredient.percentEstimate))
                    }
                }
            }
        }
        return emulsifiers
    }

    private fun populateEmulsifiersTable(emulsifiers: List<Pair<String, Double>>) {
        // Clear existing rows except the header
        while (binding.emulsifiersTable.childCount > 1) {
            binding.emulsifiersTable.removeViewAt(1)
        }

        if (emulsifiers.isNotEmpty()) {
            emulsifiers.forEachIndexed { index, (name, percent) ->
                val row = TableRow(this)
                row.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                val backgroundColor = if (index % 2 == 0) "#E0F7FA" else "#FFFFFF"
                row.setBackgroundColor(Color.parseColor(backgroundColor))
                row.setPadding(16, 24, 16, 24)

                // Emulsifier name
                val nameView = TextView(this).apply {
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    text = name
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@ProductDetailsActivity, android.R.color.black))
                }

                // Percentage
                val percentView = TextView(this).apply {
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    text = "%.1f%%".format(percent)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@ProductDetailsActivity, android.R.color.black))
                    gravity = android.view.Gravity.END
                }

                row.addView(nameView)
                row.addView(percentView)
                binding.emulsifiersTable.addView(row)
            }
        } else {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            row.setPadding(16, 24, 16, 24)

            val textView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = "None"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@ProductDetailsActivity, android.R.color.black))
            }

            row.addView(textView)
            binding.emulsifiersTable.addView(row)
        }
    }

    // Helper function to calculate consumption limits
    private fun calculateConsumptionLimits(nutriments: Nutriments, servingSize: String?): List<ConsumptionLimit> {
        // Parse serving size (e.g., "50g" → 50.0, "330ml" → 330.0)
        val servingSizeValue = servingSize?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 100.0

        // Convert per 100g to per serving
        val sugarPerServing = (nutriments.sugars_100g?.toDouble() ?: 0.0) * (servingSizeValue / 100.0)
        val sodiumPerServing = (nutriments.salt_100g?.toDouble() ?: 0.0) * (servingSizeValue / 100.0)
        val satFatPerServing = (nutriments.saturated_fat_100g?.toDouble() ?: 0.0) * (servingSizeValue / 100.0)

        return nutrientLimits.map { limit ->
            val perServing = when (limit.name) {
                "Sugar" -> sugarPerServing
                "Sodium" -> sodiumPerServing
                "Saturated Fat" -> satFatPerServing
                else -> 0.0
            }
            ConsumptionLimit(
                nutrient = limit.name,
                weeklyServings = if (perServing > 0) (limit.weeklyLimitGrams / perServing).coerceAtMost(100.0) else Double.MAX_VALUE,
                monthlyServings = if (perServing > 0) (limit.monthlyLimitGrams / perServing).coerceAtMost(400.0) else Double.MAX_VALUE
            )
        }
    }

    // Helper function to populate consumption limits table
    private fun populateConsumptionLimitsTable(limits: List<ConsumptionLimit>) {
        binding.consumptionLimitsTable.isVisible = true
        limits.forEach { limit ->
            val weeklyTextView = when (limit.nutrient) {
                "Sugar" -> binding.sugarWeeklyLimit
                "Sodium" -> binding.sodiumWeeklyLimit
                "Saturated Fat" -> binding.satFatWeeklyLimit
                else -> null
            }
            val monthlyTextView = when (limit.nutrient) {
                "Sugar" -> binding.sugarMonthlyLimit
                "Sodium" -> binding.sodiumMonthlyLimit
                "Saturated Fat" -> binding.satFatMonthlyLimit
                else -> null
            }
            weeklyTextView?.let {
                it.text = if (limit.weeklyServings < Double.MAX_VALUE) "${limit.weeklyServings.toInt()} packets" else "N/A"
                it.setTextColor(if (limit.weeklyServings < 5) Color.RED else Color.BLACK)
            }
            monthlyTextView?.let {
                it.text = if (limit.monthlyServings < Double.MAX_VALUE) "${limit.monthlyServings.toInt()} packets" else "N/A"
                it.setTextColor(if (limit.monthlyServings < 20) Color.RED else Color.BLACK)
            }
        }
        // Fade-in animation for table
        binding.consumptionLimitsTable.alpha = 0f
        binding.consumptionLimitsTable.animate().alpha(1f).setDuration(500).start()
    }

    // Helper function to populate ingredients table
    private fun populateIngredientsTable(ingredients: String) {
        while (binding.ingredientsTable.childCount > 1) {
            binding.ingredientsTable.removeViewAt(1)
        }
        val ingredientsList = if (ingredients.contains("<span")) {
            Html.fromHtml(ingredients, Html.FROM_HTML_MODE_COMPACT).toString().split(",").map { it.trim() }
        } else {
            ingredients.split(",").map { it.trim() }
        }
        ingredientsList.forEachIndexed { index, ingredient ->
            val backgroundColor = if (index % 2 == 0) "#E0F7FA" else "#FFFFFF"
            val capitalizedIngredient = ingredient.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
            addIngredientRow(capitalizedIngredient, backgroundColor)
        }
    }

    private fun addIngredientRow(ingredient: String, backgroundColor: String = "#FFFFFF") {
        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.setBackgroundColor(Color.parseColor(backgroundColor))
        row.setPadding(16, 24, 16, 24)

        val textView = TextView(this)
        textView.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
        textView.text = ingredient
        textView.textSize = 14f
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        row.addView(textView)
        binding.ingredientsTable.addView(row)
    }

    private fun setNutriScoreBadgeColor(nutriScore: String) {
        val color = when (nutriScore) {
            "A" -> "#28A745"
            "B" -> "#85BB2F"
            "C" -> "#F4C430"
            "D" -> "#F49A30"
            "E" -> "#D81E05"
            else -> "#808080"
        }
        binding.nutriScoreBadge.background.setTint(Color.parseColor(color))
    }

    private fun populateLabels(labels: List<String>) {
        binding.labelsContainer.removeAllViews()
        labels.forEach { label ->
            val tagView = LayoutInflater.from(this).inflate(R.layout.label_tag, binding.labelsContainer, false) as TextView
            tagView.text = label
            binding.labelsContainer.addView(tagView)
        }
    }
}