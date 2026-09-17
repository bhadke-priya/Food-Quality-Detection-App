package com.example.eatsure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eatsure.databinding.ItemScanBinding
import android.content.Intent

class ScanHistoryAdapter : RecyclerView.Adapter<ScanHistoryAdapter.ScanViewHolder>() {

    private val scans = mutableListOf<ScanItem>()

    fun submitList(newScans: List<ScanItem>) {
        scans.clear()
        scans.addAll(newScans)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding = ItemScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(scans[position])
    }

    override fun getItemCount(): Int = scans.size

    class ScanViewHolder(private val binding: ItemScanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scan: ScanItem) {
            scan.imageUrl?.let { url ->
                Glide.with(binding.ivProductImage.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_error)
                    .into(binding.ivProductImage)
            } ?: run {
                binding.ivProductImage.setImageResource(R.drawable.placeholder_image)
            }
            binding.tvProductName.text = scan.productName
            binding.tvNutriScore.text = scan.nutriScore?.let { "Nutri-Score: ${it.uppercase()}" } ?: "Nutri-Score: N/A"
            binding.tvTimestamp.text = scan.timestamp.let { "Date & Time: $it" }
            // Make item clickable to open ProductDetailsActivity
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra("barcode", scan.barcode)
                context.startActivity(intent)
            }
        }
    }
}