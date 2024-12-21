package com.example.momentory

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ItemTimeCapsuleBinding
import com.example.momentory.databinding.ItemTimecapsuleFriendsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TimeCapsuleItem(
    val capsuleId: String,
    val releaseDate: Date,
    val createDate: Date,
    val imageRes: Int?,
    val friends: List<String>
)

class TimeCapsuleAdapter(
    private val items: List<TimeCapsuleItem>,
    private val onItemClick: (TimeCapsuleItem) -> Unit
) : RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder>() {

    inner class TimeCapsuleViewHolder(private val binding: ItemTimeCapsuleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeCapsuleItem) {
            binding.capsuleReleaseDate.text = "~${DateUtils.formatDateWithYear(item.releaseDate)}"
            val dDayText = calculateDDay(item.releaseDate)
            binding.capsuleCreateDate.text = dDayText // D-day 텍스트 설정

            setupFriendsList(item.friends)

            if (item.releaseDate.after(Date())) {
                binding.capsuleLock.visibility = View.VISIBLE
            } else {
                binding.capsuleLock.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun setupFriendsList(friends: List<String>) {
            val innerAdapter = FriendsAdapter(friends)
            binding.capsuleFriendsList.layoutManager = LinearLayoutManager(
                itemView.context, LinearLayoutManager.HORIZONTAL, false
            )
            binding.capsuleFriendsList.adapter = innerAdapter
        }

        private fun calculateDDay(releaseDate: Date): String {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

            val todayDate = dateFormat.format(Date()).toInt()
            val releaseDate = dateFormat.format(releaseDate).toInt()

            val dDay = releaseDate - todayDate
            return when {
                dDay > 0 -> "D-$dDay"       // D-3, D-2, D-1
                dDay == 0 -> "D-day"        // D-day
                else -> "D+${-dDay}"        // D+1, D+2
            }
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

class FriendsAdapter(private val friends: List<String>) :
    RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    inner class FriendsViewHolder(private val binding: ItemTimecapsuleFriendsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val db = FirebaseFirestore.getInstance()

        fun bind(friendId: String) {
            db.collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val profileImageUrl = document.getString("profileImage")
                        if (profileImageUrl != null) {
                            Glide.with(binding.root.context)
                                .load(profileImageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.baseline_person_24) // 로딩 중 표시할 이미지
                                .into(binding.friendProfileImage)
                        } else {
                            binding.friendProfileImage.setImageResource(R.drawable.baseline_person_24)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FriendsAdapter", "Error getting document: ", exception)
                    binding.friendProfileImage.setImageResource(R.drawable.baseline_person_24)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val binding = ItemTimecapsuleFriendsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size
}
