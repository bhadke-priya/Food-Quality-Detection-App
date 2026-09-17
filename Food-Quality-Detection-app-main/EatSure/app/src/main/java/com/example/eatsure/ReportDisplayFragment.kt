package com.example.eatsure

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eatsure.databinding.FragmentReportDisplayBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.content.res.ColorStateList

class ReportDisplayFragment : Fragment() {

    private var _binding: FragmentReportDisplayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels({ requireActivity() })
    private val TAG = "ReportDisplayFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentReportDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        // Initialize RecyclerView adapters
        val risksAdapter = ListItemAdapter()
        val recommendationsAdapter = ListItemAdapter()

        binding.rvRisks.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = risksAdapter
        }

        binding.rvRecommendations.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = recommendationsAdapter
        }

        // Observe report and fetch additional data if needed
        viewModel.report.observe(viewLifecycleOwner) { healthReport ->
            if (healthReport != null) {
                Log.d(TAG, "Health report observed: $healthReport")
                // Fetch full SavedHealthReport from Firebase to get demographics and consumption items
                fetchSavedHealthReport { savedReport ->
                    if (savedReport != null) {
                        displayReport(savedReport)
                    } else {
                        Snackbar.make(binding.root, "Failed to load full report details", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                Snackbar.make(binding.root, "No health report available", Snackbar.LENGTH_LONG).show()
            }
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Log.e(TAG, "Error: $error")
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }

        // Set up button listeners
        binding.btnBackToInput.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnGenerateReport.setOnClickListener {
            // Navigate back to ReportInputFragment to generate a new report
            requireActivity().onBackPressed()
        }
    }

    private fun fetchSavedHealthReport(callback: (SavedHealthReport?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e(TAG, "User not logged in")
            callback(null)
            return
        }

        val database = FirebaseDatabase.getInstance()
        database.getReference("users")
            .child(user.uid)
            .child("health_reports")
            .orderByChild("timestamp")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Fetching latest health report")
                    val savedReport = snapshot.children.firstOrNull()?.getValue(SavedHealthReport::class.java)
                    if (savedReport != null) {
                        Log.d(TAG, "Fetched saved report: ${savedReport.formattedDate}")
                        callback(savedReport)
                    } else {
                        Log.w(TAG, "No saved report found")
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch saved report: ${error.message}")
                    callback(null)
                }
            })
    }

    private fun displayReport(savedReport: SavedHealthReport) {
        // Display demographics
        val demographics = savedReport.demographics
        binding.tvAge.text = "${demographics.age} years"
        binding.tvGender.text = demographics.gender
        binding.tvHeight.text = "${demographics.height} cm"
        binding.tvWeight.text = "${demographics.weight} kg"
        binding.tvActivityLevel.text = demographics.activityLevel

        // Display overall score
        val overallScore = savedReport.healthReport.overallScore
        binding.tvOverallScore.text = overallScore.toString()
        binding.progressOverallScore.progress = overallScore
        binding.tvScoreDescription.text = when {
            overallScore >= 80 -> "Excellent"
            overallScore >= 60 -> "Good"
            overallScore >= 40 -> "Fair"
            else -> "Poor"
        }
        val scoreColor = when {
            overallScore >= 80 -> R.color.score_good
            overallScore >= 60 -> R.color.score_good
            overallScore >= 40 -> R.color.risk_color
            else -> R.color.risk_color
        }
        binding.tvScoreDescription.setTextColor(ContextCompat.getColor(requireContext(), scoreColor))
        binding.tvOverallScore.setTextColor(ContextCompat.getColor(requireContext(), scoreColor))
        binding.progressOverallScore.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), scoreColor))

        // Display nutrient summary

        val nutrientSummary = savedReport.healthReport.nutrientSummary
        Log.d(TAG, "Nutrient Summary: $nutrientSummary") // Add logging
        binding.tvCaloriesValue.text = "${nutrientSummary["calories"] ?: 0.0} kcal"
        binding.tvFatValue.text = "${nutrientSummary["fat"] ?: 0.0} g"
        binding.tvSaturatedFatValue.text = "${nutrientSummary["saturated_fat"] ?: 0.0} g"
        binding.tvCarbohydratesValue.text = "${nutrientSummary["carbohydrates"] ?: 0.0} g"
        binding.tvSugarsValue.text = "${nutrientSummary["sugars"] ?: 0.0} g"
        binding.tvProteinsValue.text = "${nutrientSummary["proteins"] ?: 0.0} g"
        binding.tvFiberValue.text = "${nutrientSummary["fiber"] ?: 0.0} g"
        binding.tvSaltValue.text = "${nutrientSummary["salt"] ?: 0.0} g"
        binding.tvSodiumValue.text = "${nutrientSummary["sodium"] ?: 0.0} mg"

        // Display risks
        (binding.rvRisks.adapter as ListItemAdapter).submitList(savedReport.healthReport.risks)

        // Display recommendations
        (binding.rvRecommendations.adapter as ListItemAdapter).submitList(savedReport.healthReport.recommendations)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}