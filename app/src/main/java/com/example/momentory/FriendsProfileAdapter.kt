package com.example.momentory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ItemFriendProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

// FriendProfile 데이터 클래스
data class FriendProfile(
    val id: String,             // 친구 UID
    val profileImageUrl: String // 프로필 이미지 URL
)

// FriendsProfileAdapter: 친구 프로필 RecyclerView 어댑터
class FriendsProfileAdapter(
    private var friendsUidList: List<String> // 친구 UID 리스트
) : RecyclerView.Adapter<FriendsProfileAdapter.FriendsViewHolder>() {

    private val friendsList = mutableListOf<FriendProfile>() // 친구 프로필 정보 리스트
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchFriendsProfiles()
    }

    inner class FriendsViewHolder(private val binding: ItemFriendProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendProfile) {
            Glide.with(binding.root.context)
                .load(friend.profileImageUrl)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(binding.friendProfileImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val binding = ItemFriendProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        holder.bind(friendsList[position])
    }

    override fun getItemCount(): Int = friendsList.size

    private fun fetchFriendsProfiles() {
        friendsList.clear()
        for (uid in friendsUidList) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    friendsList.add(FriendProfile(uid, profileImageUrl))
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    println("Error fetching profile for UID $uid: ${e.message}")
                }
        }
    }

    // 친구 리스트 업데이트 함수 추가
    fun updateFriendsList(newFriendsList: List<FriendProfile>) {
        friendsList.clear()
        friendsList.addAll(newFriendsList)
        notifyDataSetChanged()
    }
}
