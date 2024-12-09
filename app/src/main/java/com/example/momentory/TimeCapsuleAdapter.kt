package com.example.momentory

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ItemTimeCapsuleBinding
import com.example.momentory.databinding.ItemTimecapsuleFriendsBinding
import java.util.Calendar
import java.util.Date


data class TimeCapsuleItem(
    val releaseDate: Date,
    val createDate: Date,
    val imageRes: Int?,
    val friends: List<String>
)

data class Time(
    val year: Int,
    val month: Int,
    val day: Int
)

class TimeCapsuleAdapter(
    private val items: List<TimeCapsuleItem>,
    private val onItemClick: (TimeCapsuleItem) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder>() {

    inner class TimeCapsuleViewHolder(private val binding: ItemTimeCapsuleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimeCapsuleItem) {
            val releaseTime: Time = timeFormat(item.releaseDate)
            val createTime: Time = timeFormat(item.createDate)
            binding.capsuleReleaseDate.text =
                "~${releaseTime.year}년 ${releaseTime.month}월 ${releaseTime.day}일"
            binding.capsuleCreateDate.text =
                "${createTime.month}월 ${createTime.day}일"

            setupFriendsList(item.friends)

            if (item.releaseDate.after(Date())) {
                binding.capsuleLock.visibility = View.VISIBLE
            } else {
                binding.capsuleLock.visibility = View.GONE
                if (item.imageRes != null) {
                    binding.capsuleImage.setImageResource(item.imageRes)
                }
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

        fun timeFormat(date: Date): Time {
//            Tue Oct 15 00:00:00 GMT 2024
            val calendar = Calendar.getInstance()
            calendar.time = date

            // 요소 추출
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 0부터 시작하므로 +1 필요
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return Time(
                year = year,
                month = month,
                day = day
            )
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

        fun bind(friend: String) {
//            friend : 친구 ID List, -> 스토리지 연동하여 프로필 이미지 가져오기
            binding.friendProfileImage.setImageResource(R.drawable.character)
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

