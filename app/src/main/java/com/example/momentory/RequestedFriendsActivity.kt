package com.example.momentory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.example.momentory.databinding.ActivityRequestedFriendsBinding
import com.example.momentory.databinding.FriendsRequestListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.sql.Date

class MyViewHolder(val binding: FriendsRequestListBinding) :
    RecyclerView.ViewHolder(binding.root)

class MyAdapter(
    private val names: MutableList<String>,
    private val messages: MutableList<String>,
    private val fromUserIds: MutableList<String>
) : RecyclerView.Adapter<MyViewHolder>() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    override fun getItemCount(): Int = names.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            FriendsRequestListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.requestedFriendName.text = names[position]
        holder.binding.requestedFriendMessage.text = messages[position]

        // Firestore에서 profileImage URL 가져오기
        val userId = fromUserIds[position]
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImage")
                if (!profileImageUrl.isNullOrEmpty()) {
                    // Glide를 사용하여 이미지를 불러와 설정
                    Glide.with(holder.itemView.context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.baseline_person_24) // 기본 이미지
                        .error(R.drawable.baseline_person_24) // 오류 발생 시 기본 이미지
                        .into(holder.binding.requestedFriendProfileImage)
                } else {
                    // profileImage 필드가 없거나 URL이 비어 있는 경우 기본 이미지 설정
                    holder.binding.requestedFriendProfileImage.setImageResource(R.drawable.baseline_person_24)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RequestedFriendsActivity", "Error loading profile image for userId: $userId", e)
                holder.binding.requestedFriendProfileImage.setImageResource(R.drawable.baseline_person_24) // 실패 시 기본 이미지 설정
            }

        holder.binding.friendAcceptBtn.setOnClickListener {
            acceptFriend(position)
            removeRequest(position)
        }
        holder.binding.friendRejectBtn.setOnClickListener {
            removeRequest(position)
        }
    }

    private fun removeRequest(position: Int) {
        val senderId = fromUserIds[position] // senderId를 사용
        Log.d("senderID", senderId)
        Log.d("RequestedFriendsActivity", "sender ID : $senderId")

        // 현재 사용자의 friendRequestsReceived에서 요청 삭제
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .collection("friendRequestsReceived")
                .document(senderId)  // senderId를 문서 ID로 사용
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Request removed from friendRequestsReceived")

                    // senderId의 friendRequestsSent에서 해당 요청 삭제
                    db.collection("users").document(senderId)
                        .collection("friendRequestsSent")
                        .document(currentUserId)  // currentUserId를 문서 ID로 사용
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Request removed from friendRequestsSent")

                            // 데이터에서 해당 요청 제거
                            fromUserIds.removeAt(position)
                            messages.removeAt(position)
                            names.removeAt(position)

                            // 어댑터에 데이터 변경을 알리고 리사이클러뷰 갱신
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error removing request from friendRequestsSent", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error removing request from friendRequestsReceived", e)
                }
        }
    }

    private fun acceptFriend(position: Int) {
        val senderId = fromUserIds[position] // senderId를 사용
        Log.d("senderID", senderId)

        // 친구 리스트에 추가
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .update("friends", FieldValue.arrayUnion(senderId))
                .addOnSuccessListener {
                    Log.d("Firestore", "Friend added to friends list")
                    // 수락 후 친구 요청 삭제
                    removeRequest(position)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error accepting friend", e)
                }
        }

        db.collection("users").document(senderId)
            .update("friends", FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                Log.d("Firestore", "Friend added to friends list")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error accepting friend", e)
            }
    }
}

class RequestedFriendsActivity : AppCompatActivity() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityRequestedFriendsBinding by lazy {
            ActivityRequestedFriendsBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(currentUserId)
            .collection("friendRequestsReceived")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                val names = mutableListOf<String>()
                val messages = mutableListOf<String>()
                val fromUserIds = mutableListOf<String>()

                for (document in documents) {
                    val fromUserId = document.getString("fromUserId") ?: continue
                    val message = document.getString("message") ?: "메시지 없음"

                    db.collection("users").document(fromUserId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            val userName = userDocument.getString("name") ?: "이름 없음"

                            // 가져온 사용자 이름을 names 리스트에 추가
                            fromUserIds.add(fromUserId)
                            names.add(userName)
                            messages.add(message)

                            // 모든 친구 요청을 불러온 후 RecyclerView 설정
                            if (names.size == documents.size()) {
                                binding.requestedFriendsRecyclerView.apply {
                                    layoutManager =
                                        LinearLayoutManager(this@RequestedFriendsActivity)
                                    adapter = MyAdapter(names, messages, fromUserIds)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RequestedFriendsActivity", "Error fetching user name", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "친구 요청을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("RequestedFriendsActivity", "Error fetching friend requests", exception)
            }
        if (binding.requestedFriendsRecyclerView.adapter?.itemCount == 0)
            binding.noFriendsText.visibility = ViewGroup.VISIBLE
    }
}
