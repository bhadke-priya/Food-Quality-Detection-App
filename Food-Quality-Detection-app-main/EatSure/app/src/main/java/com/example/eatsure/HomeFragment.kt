package com.example.eatsure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eatsure.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val scannerLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Snackbar.make(binding.root, "Scan cancelled", Snackbar.LENGTH_SHORT).show()
        } else {
            val barcode = result.contents
            viewModel.saveScan(barcode) // Save scan to Firebase
            // Launch ProductDetailsActivity with the scanned barcode
            val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
            intent.putExtra("barcode", barcode)
            startActivity(intent)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up the Scan button click listener
        binding.scan.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions()
                    .setPrompt("Scan")
                    .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                    .setCameraId(0) // Use back camera
            )
        }

        // Navigate to ProfileFragment
        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.nav_profile)
        }

        // Observe scan save status
        viewModel.scanSaved.observe(viewLifecycleOwner) { saved: Boolean ->
            if (saved) {
                Snackbar.make(binding.root, "Scan saved", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error: String ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}