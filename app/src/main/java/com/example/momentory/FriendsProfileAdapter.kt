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

        private val db = FirebaseFirestore.getInstance()

        fun bind(friend: FriendProfile) {
            // 프로필 이미지 URL이 있을 경우 Glide로 로드
            if (friend.profileImageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(friend.profileImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(binding.friendProfileImage)
            } else {
                // Firestore에서 친구의 프로필 이미지 URL을 동적으로 가져오기
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
                            // Firestore에 이미지가 없을 경우 기본 이미지 표시
                            binding.friendProfileImage.setImageResource(R.drawable.baseline_person_24)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Firestore 읽기 실패 시 기본 이미지 표시
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



    // 친구 리스트 업데이트 함수 추가
    fun updateFriendsList(newFriendsList: List<FriendProfile>) {
        friendsList.clear()
        friendsList.addAll(newFriendsList)
        notifyDataSetChanged()
    }
}
