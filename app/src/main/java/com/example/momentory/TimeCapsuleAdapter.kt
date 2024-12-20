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
import java.util.Calendar
import java.util.Date

data class TimeCapsuleItem(
    val capsuleId: String,
    val releaseDate: Date,
    val createDate: Date,
    val imageRes: Int?,
    val friends: List<String>
)

class TimeCapsuleAdapter(
    private val items: List<TimeCapsuleItem>,
    private val onItemClick: (TimeCapsuleItem) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder>() {

    inner class TimeCapsuleViewHolder(private val binding: ItemTimeCapsuleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeCapsuleItem) {
            // DateUtils를 사용하여 날짜 포맷팅
            binding.capsuleReleaseDate.text = "~${DateUtils.formatDateWithYear(item.releaseDate)}"
//            binding.capsuleCreateDate.text = DateUtils.formatDateWithoutYear(item.createDate)
            val dDayText = calculateDDay(item.releaseDate)
            binding.capsuleCreateDate.text = dDayText // D-day 텍스트 설정

            setupFriendsList(item.friends)

            if (item.releaseDate.after(Date())) {
                binding.capsuleLock.visibility = View.VISIBLE
            } else {
                binding.capsuleLock.visibility = View.GONE
            }

            // 아이템 클릭 이벤트 설정
            binding.root.setOnClickListener {
                onItemClick(item) // 클릭된 아이템 전달
            }
        }

        private fun setupFriendsList(friends: List<String>) {
            // 친구 리스트를 RecyclerView에 표시
            val innerAdapter = FriendsAdapter(friends)
            binding.capsuleFriendsList.layoutManager = LinearLayoutManager(
                itemView.context, LinearLayoutManager.HORIZONTAL, false
            )
            binding.capsuleFriendsList.adapter = innerAdapter
        }

        private fun calculateDDay(releaseDate: Date): String {
            val currentDate = Date() // 오늘 날짜
            val diffInMillis = releaseDate.time - currentDate.time
            val dDay = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() // 남은 일수

            return when {
                dDay > 0 -> "D-$dDay"       // D-3, D-2, D-1
                dDay == 0 -> "D-day"        // D-day (오늘이 해제일)
                else -> "D+${-dDay}"        // D+1, D+2 (이미 지난 날짜)
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
            // Firestore에서 친구의 프로필 이미지 URL을 가져오기
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
                            // 프로필 이미지가 없다면 기본 이미지 사용
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
        val binding = ItemTimecapsuleFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size
}
