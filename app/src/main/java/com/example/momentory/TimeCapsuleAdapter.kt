package com.example.momentory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ItemTimeCapsuleBinding


data class TimeCapsuleItem(
    val releaseDate: String,
    val description: String,
    val imageRes: Int
)

class TimeCapsuleAdapter(private val items: List<TimeCapsuleItem>) :
    RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder>() {


    inner class TimeCapsuleViewHolder(private val binding: ItemTimeCapsuleBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: TimeCapsuleItem) {

            binding.capsuleReleaseDate.text = item.releaseDate
            binding.capsuleDescription.text = item.description


            binding.capsuleImageContainer.setBackgroundResource(R.drawable.rounded_lock_background)


            binding.capsuleImage.setImageResource(item.imageRes)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCapsuleViewHolder {
        val binding = ItemTimeCapsuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimeCapsuleViewHolder(binding)
    }


    override fun onBindViewHolder(holder: TimeCapsuleViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun getItemCount(): Int = items.size
}
