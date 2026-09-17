package com.example.eatsure

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Headers
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.util.Log
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Data class for Gemini API response
data class GeminiApiResponse(
    val candidates: List<Candidate>
) {
    data class Candidate(
        val content: Content
    ) {
        data class Content(
            val parts: List<Part>
        ) {
            data class Part(
                val text: String
            )
        }
    }
}

// Data class for the request body to Gemini API
data class GeminiRequest(
    val contents: List<Content>
) {
    data class Content(
        val parts: List<Part>
    ) {
        data class Part(
            val text: String
        )
    }
}

// Retrofit interface for Gemini API
interface GeminiApiInterface {
    @Headers("Content-Type: application/json")
    @POST("models/gemini-1.5-flash:generateContent")
    suspend fun generateReport(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String
    ): GeminiApiResponse
}

// GeminiApiService class to handle API calls
class GeminiApiService {

    private val TAG = "GeminiApiService"

    // Create OkHttpClient with timeout configuration AND logging interceptor for debugging
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Increased timeout
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
//        .addInterceptor(HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: GeminiApiInterface = retrofit.create(GeminiApiInterface::class.java)

    suspend fun generateHealthReport(demographics: UserDemographics, consumptionItems: List<ConsumptionItem>): HealthReport {
        // Format the consumption data
        val consumptionData = consumptionItems.joinToString("\n") { item ->
            """
- Product Name: ${item.productName}
- Barcode: ${item.barcode}
- Nutrients (per 100g):
  - Energy: ${item.nutrients["energy_kcal_100g"] ?: 0.0} kcal
  - Fat: ${item.nutrients["fat_100g"] ?: 0.0} g
  - Saturated Fat: ${item.nutrients["saturated_fat_100g"] ?: 0.0} g
  - Carbohydrates: ${item.nutrients["carbohydrates_100g"] ?: 0.0} g
  - Sugars: ${item.nutrients["sugars_100g"] ?: 0.0} g
  - Proteins: ${item.nutrients["proteins_100g"] ?: 0.0} g
  - Salt: ${item.nutrients["salt_100g"] ?: 0.0} g
  - Fiber: ${item.nutrients["fiber_100g"] ?: 0.0} g
  - Sodium: ${item.nutrients["sodium_100g"] ?: 0.0} g
- Consumption Frequency: ${item.frequency}
- Serving Size: ${item.servingSize} g per serving
            """.trimIndent()
        }

        // Simplified and more focused prompt
        val prompt = """
As a nutrition expert, Generate a concise, impactful, and highly personalized health report focusing 
exclusively on the user's packaged food consumption habits. The report should educate the user on the 
health impact of these specific products and offer actionable recommendations . The tone should be 
encouraging and informative, not judgmental.Now analyze the following data and provide a health assessment:

USER PROFILE:
- Age: ${demographics.age} years
- Gender: ${demographics.gender}
- Height: ${demographics.height} cm
- Weight: ${demographics.weight} kg
- Activity Level: ${demographics.activityLevel}

FOOD CONSUMPTION:
$consumptionData

Please provide a JSON response with daily nutrient intake calculations, health risks, and recommendations.

Response format (JSON only):
{
  "nutrientSummary": {
    "calories": 0,
    "fat": 0,
    "saturated_fat": 0,
    "carbohydrates": 0,
    "sugars": 0,
    "proteins": 0,
    "salt": 0,
    "sodium": 0,
    "fiber": 0
  },
  "risks": ["health risk 1", "health risk 2"],
  "recommendations": ["recommendation 1", "recommendation 2"],
  "overallScore": 75
}
        """.trimIndent()

        // Create the request
        val request = GeminiRequest(
            contents = listOf(
                GeminiRequest.Content(
                    parts = listOf(
                        GeminiRequest.Content.Part(text = prompt)
                    )
                )
            )
        )

        // Log the request
        Log.d(TAG, "Sending Gemini API request")
        Log.d(TAG, "Request body: ${Gson().toJson(request)}")

        // Get API key from BuildConfig or environment - NEVER hardcode in production!
        val apiKey = getApiKey()

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key not set!")
            throw Exception("API key not configured. Please set your Gemini API key.")
        }

        // Call the Gemini API
        val response = try {
            Log.d(TAG, "Making API call to Gemini...")
            api.generateReport(request, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed: ${e.message}", e)

            // Provide more specific error information
            when {
                e.message?.contains("HTTP 400") == true -> {
                    throw Exception("Bad request - Invalid request format or parameters")
                }
                e.message?.contains("HTTP 401") == true -> {
                    throw Exception("Unauthorized - Check your API key")
                }
                e.message?.contains("HTTP 403") == true -> {
                    throw Exception("Forbidden - API key lacks permissions or quota exceeded")
                }
                e.message?.contains("HTTP 429") == true -> {
                    throw Exception("Rate limit exceeded - please try again later")
                }
                e.message?.contains("timeout") == true -> {
                    throw Exception("Request timeout - please check your network connection")
                }
                e.message?.contains("Unable to resolve host") == true -> {
                    throw Exception("Network error - please check your internet connection")
                }
                else -> {
                    throw Exception("API call failed: ${e.message}")
                }
            }
        }

        // Log the raw response
        Log.d(TAG, "Gemini API response received")
        Log.d(TAG, "Response: ${Gson().toJson(response)}")

        // Extract the generated text from the response
        val generatedText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No content generated by Gemini API - empty response")

        Log.d(TAG, "Generated text: $generatedText")

        // Clean the response text (remove markdown formatting if present)
        val cleanedText = generatedText
            .replace("```json", "")
            .replace("```", "")
            .replace("\\n", "")
            .trim()

        Log.d(TAG, "Cleaned text: $cleanedText")

        // Parse the generated text (JSON string) into HealthReport
        return try {
            val gson = Gson()
            val healthReport = gson.fromJson(cleanedText, HealthReport::class.java)
            Log.d(TAG, "Parsed HealthReport successfully")

            // Validate the parsed report
            if (healthReport.nutrientSummary.isEmpty()) {
                throw Exception("Invalid report structure - empty nutrient summary")
            }

            healthReport
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini API response: ${e.message}", e)
            Log.e(TAG, "Raw response text: $generatedText")
            Log.e(TAG, "Cleaned response text: $cleanedText")

            // Try to extract JSON from the response if it's wrapped in other text
            val jsonStartIndex = cleanedText.indexOf("{")
            val jsonEndIndex = cleanedText.lastIndexOf("}") + 1

            if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
                val extractedJson = cleanedText.substring(jsonStartIndex, jsonEndIndex)
                Log.d(TAG, "Extracted JSON: $extractedJson")

                try {
                    val gson = Gson()
                    val healthReport = gson.fromJson(extractedJson, HealthReport::class.java)
                    Log.d(TAG, "Successfully parsed extracted JSON")
                    return healthReport
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to parse extracted JSON: ${ex.message}")
                }
            }

            // Fallback to a default HealthReport to avoid crashing
            HealthReport(
                nutrientSummary = mapOf(
                    "calories" to 0.0,
                    "fat" to 0.0,
                    "saturated_fat" to 0.0,
                    "carbohydrates" to 0.0,
                    "sugars" to 0.0,
                    "proteins" to 0.0,
                    "salt" to 0.0,
                    "sodium" to 0.0,
                    "fiber" to 0.0
                ),
                risks = listOf("Unable to analyze data - parsing error"),
                recommendations = listOf("Please try again later"),
                overallScore = 0
            )
        }
    }

    // Secure way to get API key - implement this based on your preference
    private fun getApiKey(): String {
        return "AIzaSyDTAmitK1gBa7T0jLFkeAxjG9CaMsRcICM"
    }

}