package com.example.eatsure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eatsure.R

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var backButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)

        webView = view.findViewById(R.id.webview)
        backButton = view.findViewById(R.id.back_button)

        // Enable JavaScript, DOM storage, and debugging
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)

        // Get FSSAI number from arguments
        val fssaiNumber = arguments?.getString("fssaiNumber") ?: ""

        // Set WebViewClient to handle page loading and apply JavaScript
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url?.contains("foscos.fssai.gov.in") == true) {
                    try {
                        // Read JavaScript file and replace placeholder
                        var jsCode = requireContext().assets.open("fssai_script.js")
                            .bufferedReader()
                            .use { it.readText() }
                        jsCode = jsCode.replace("__FSSAI_NUMBER__", fssaiNumber)
                        view?.evaluateJavascript(jsCode, null)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error loading JavaScript: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(requireContext(), "Error loading page: $description", Toast.LENGTH_LONG).show()
            }
        }

        // Add JavaScript interface for Toast messages
        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun showToast(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }, "Android")

        // Load FOSCOS website
        webView.loadUrl("https://foscos.fssai.gov.in")

        // Back button to return to FssaiVerificationFragment
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}