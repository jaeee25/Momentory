package com.example.momentory

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ItemFriendProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

// FriendProfile 데이터 클래스
data class FriendProfile(
    val id: String,
    val profileImageUrl: String
)

// FriendsProfileAdapter: 친구 프로필 RecyclerView 어댑터
class FriendsProfileAdapter(
    private var friendsUidList: List<String> // 친구 UID 리스트
) : RecyclerView.Adapter<FriendsProfileAdapter.FriendsViewHolder>() {

    private val friendsList = mutableListOf<FriendProfile>()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchFriendsProfiles()
    }

    inner class FriendsViewHolder(private val binding: ItemFriendProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val db = FirebaseFirestore.getInstance()

        fun bind(friend: FriendProfile) {
            // 프로필 이미지 URL이 있을 경우
            if (friend.profileImageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(friend.profileImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(binding.friendProfileImage)
            } else {
                // Firestore에서 친구의 프로필 이미지
                db.collection("users")
                    .document(friend.id)
                    .get()
                    .addOnSuccessListener { document ->
                        val profileImageUrl = document.getString("profileImage")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(binding.root.context)
                                .load(profileImageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.baseline_person_24)
                                .error(R.drawable.baseline_person_24)
                                .into(binding.friendProfileImage)
                        } else {
                            binding.friendProfileImage.setImageResource(R.drawable.baseline_person_24)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FriendsProfileAdapter", "Error fetching profile for UID ${friend.id}", exception)
                        binding.friendProfileImage.setImageResource(R.drawable.baseline_person_24)
                    }
            }
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
                    val profileImageUrl = document.getString("profileImage") ?: ""
                    friendsList.add(FriendProfile(uid, profileImageUrl))
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    println("Error fetching profile for UID $uid: ${e.message}")
                }
        }
    }




    fun updateFriendsList(newFriendsList: List<FriendProfile>) {
        friendsList.clear()
        friendsList.addAll(newFriendsList)
        notifyDataSetChanged()
    }
}
