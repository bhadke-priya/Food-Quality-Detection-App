package com.example.eatsure

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eatsure.databinding.ItemConsumptionFrequencyBinding

class ConsumptionFrequencyAdapter : RecyclerView.Adapter<ConsumptionFrequencyAdapter.FrequencyViewHolder>() {

    private val scans = mutableListOf<ScanItem>()
    private val frequencies = mutableMapOf<String, String>()
    private val servingSizes = mutableMapOf<String, Float>()

    fun submitList(newScans: List<ScanItem>) {
        scans.clear()
        scans.addAll(newScans)
        notifyDataSetChanged()
    }

    fun getConsumptionFrequencies(): Map<String, Pair<String, Pair<String, Float>>> {
        return frequencies.mapValues { it.key to Pair(it.value, servingSizes[it.key] ?: 100f) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrequencyViewHolder {
        val binding = ItemConsumptionFrequencyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FrequencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FrequencyViewHolder, position: Int) {
        holder.bind(scans[position], position)
    }

    override fun getItemCount(): Int = scans.size

    inner class FrequencyViewHolder(private val binding: ItemConsumptionFrequencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scan: ScanItem, position: Int) {
            // Set product name
            binding.tvProductName.text = scan.productName

            // Set product category (you can add category to ScanItem or derive from product name)
            binding.tvProductCategory.text = getProductCategory(scan.productName)

            // Set serving size in EditText (if you have etServingSize in your layout)
            binding.etServingSize?.setText(servingSizes[scan.barcode]?.toString() ?: "100")

            // Setup frequency spinner with custom styling
            val adapter = ArrayAdapter.createFromResource(
                binding.root.context,
                R.array.frequency_options,
                android.R.layout.simple_spinner_item
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            binding.spinnerFrequency.adapter = adapter

            // Set current selection if exists
            frequencies[scan.barcode]?.let { frequency ->
                val frequencyArray = binding.root.context.resources.getStringArray(R.array.frequency_options)
                val index = frequencyArray.indexOf(frequency)
                if (index >= 0) {
                    binding.spinnerFrequency.setSelection(index)
                }
            }

            // Handle frequency selection changes
            binding.spinnerFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                    val selectedFrequency = parent.getItemAtPosition(pos).toString()
                    frequencies[scan.barcode] = selectedFrequency

                    // Add visual feedback
                    animateSelection(binding.root)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Handle serving size changes (if you have etServingSize)
            binding.etServingSize?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    servingSizes[scan.barcode] = s.toString().toFloatOrNull() ?: 100f
                }
            })

            // Add entry animation
            animateItemEntry(binding.root, position)
        }

        private fun getProductCategory(productName: String): String {
            // Simple categorization logic - you can enhance this
            return when {
                productName.contains("chips", ignoreCase = true) ||
                        productName.contains("crisp", ignoreCase = true) -> "Snacks"
                productName.contains("biscuit", ignoreCase = true) ||
                        productName.contains("cookie", ignoreCase = true) -> "Biscuits & Cookies"
                productName.contains("chocolate", ignoreCase = true) ||
                        productName.contains("candy", ignoreCase = true) -> "Confectionery"
                productName.contains("juice", ignoreCase = true) ||
                        productName.contains("drink", ignoreCase = true) -> "Beverages"
                productName.contains("noodles", ignoreCase = true) ||
                        productName.contains("pasta", ignoreCase = true) -> "Instant Food"
                else -> "Packaged Food"
            }
        }

        // Add smooth animations
        private fun animateItemEntry(view: android.view.View, position: Int) {
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((position * 50).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        private fun animateSelection(view: android.view.View) {
            view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }
}

