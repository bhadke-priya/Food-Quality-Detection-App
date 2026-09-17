package com.example.eatsure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eatsure.R
import com.google.android.material.textfield.TextInputEditText

class FssaiVerificationFragment : Fragment() {

    private lateinit var fssaiInput: TextInputEditText
    private lateinit var verifyButton: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fssai_verification, container, false)

        fssaiInput = view.findViewById(R.id.fssai_input)
        verifyButton = view.findViewById(R.id.verify_button)
        verifyButton.setOnClickListener {
            val input = fssaiInput.text.toString().trim()
            if (input.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("fssaiNumber", input)
                }
                try {
                    findNavController().navigate(R.id.action_fssaiVerification_to_webView, bundle)
                    println("Navigation successful to WebViewFragment")
                } catch (e: Exception) {
                    Toast.makeText(context, "Navigation failed: ${e.message}", Toast.LENGTH_LONG).show()
                    println("Navigation failed: ${e.message}, StackTrace: ${e.stackTraceToString()}")
                }
            } else {
                Toast.makeText(context, "Please enter an FSSAI number", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}