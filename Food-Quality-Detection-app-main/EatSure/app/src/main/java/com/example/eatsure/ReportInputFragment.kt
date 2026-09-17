package com.example.eatsure

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eatsure.databinding.FragmentReportInputBinding
import com.google.android.material.snackbar.Snackbar

class ReportInputFragment : Fragment() {

    private var _binding: FragmentReportInputBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels({ requireActivity() })
    private val TAG = "ReportInputFragment"
    private var hasNavigated = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentReportInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        viewModel.clearReport()
        hasNavigated = false

        val adapter = ConsumptionFrequencyAdapter()
        binding.rvConsumptionFrequency.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

//        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
//            Log.d(TAG, "Loading state: $isLoading")
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            binding.rvConsumptionFrequency.isEnabled = !isLoading
////            binding.btnGenerateReport.isEnabled = !isLoading
////            binding.btnGenerateReport.text = if (isLoading) "Generating Report..." else "Generate Report"
//        }

        viewModel.scanHistory.observe(viewLifecycleOwner) { scans ->
            Log.d(TAG, "Scan history observed: ${scans.size} items")
            if (scans.isEmpty()) {
                binding.rvConsumptionFrequency.visibility = View.GONE
                binding.tvConsumptionFrequency.visibility = View.GONE
                binding.tvNoScans.visibility = View.VISIBLE
            } else {
                binding.rvConsumptionFrequency.visibility = View.VISIBLE
                binding.tvConsumptionFrequency.visibility = View.VISIBLE
                binding.tvNoScans.visibility = View.GONE
                adapter.submitList(scans)
            }
        }

        binding.spinnerGender.adapter = android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerActivityLevel.adapter = android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_level_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        viewModel.report.observe(viewLifecycleOwner) { report ->
            Log.d(TAG, "Report observed: $report")
            if (report != null && !hasNavigated) {
                Log.d(TAG, "Navigating to ReportDisplayFragment")
                hasNavigated = false
                try {
                    findNavController().navigate(R.id.action_report_input_to_report_display)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation failed: ${e.message}", e)
                    hasNavigated = false
                }
            }
        }

        binding.btnGenerateReport.setOnClickListener {
            Log.d(TAG, "Generate report button clicked")

            val age = binding.etAge.text.toString().toIntOrNull()
            val height = binding.etHeight.text.toString().toFloatOrNull()
            val weight = binding.etWeight.text.toString().toFloatOrNull()
            val gender = binding.spinnerGender.selectedItem.toString()
            val activityLevel = binding.spinnerActivityLevel.selectedItem.toString()

            if (age == null || height == null || weight == null) {
                Snackbar.make(binding.root, "Please fill all demographic fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (age <= 0 || height <= 0 || weight <= 0) {
                Snackbar.make(binding.root, "Please enter valid positive values", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (viewModel.scanHistory.value.isNullOrEmpty()) {
                Snackbar.make(binding.root, "No scan history available. Please scan a product first.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val frequencies = adapter.getConsumptionFrequencies()

            if (frequencies.isEmpty()) {
                Snackbar.make(binding.root, "Please set consumption frequency for at least one product", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Starting report generation with ${frequencies.size} products")

            hasNavigated = false

            viewModel.generateReport(
                age = age,
                height = height,
                weight = weight,
                gender = gender,
                activityLevel = activityLevel,
                consumptionFrequencies = frequencies
            )
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Log.e(TAG, "Error observed: $error")
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                hasNavigated = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}

