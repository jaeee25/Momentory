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
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.example.momentory.databinding.ActivityRequestedFriendsBinding
import com.example.momentory.databinding.FriendsRequestListBinding
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
        holder.binding.requestedFriendProfileImage.setImageResource(R.drawable.character)

        holder.binding.friendAcceptBtn.setOnClickListener {
            acceptFriend(position)
            removeRequest(position)
        }
        holder.binding.friendRejectBtn.setOnClickListener {
            rejectFriend(position)
            removeRequest(position)
        }
    }

    private val currentUserId = "4U2aXV9OYK5NobTnUEIX"
    private fun removeRequest(position: Int) {
        val senderId = fromUserIds[position] // 실제로는 senderId를 사용해야 함
        Log.d("RequestedFriendsActivity","sender ID : $senderId")
        db.collection("users").document(currentUserId)
            .update("friendRequestsReceived", FieldValue.arrayRemove(senderId))
            .addOnSuccessListener {
                Log.d("Firestore", "Request removed")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error removing request", e)
            }
    }

    private fun acceptFriend(position: Int) {
        val senderId = fromUserIds[position] // 실제로는 senderId를 사용해야 함
        db.collection("users").document(currentUserId)
            .update("friends", FieldValue.arrayUnion(senderId))
            .addOnSuccessListener {
                removeRequest(position) // 요청 삭제
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error accepting friend", e)
            }
    }

    private fun rejectFriend(position: Int) {
        removeRequest(position) // 친구 요청을 삭제
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

        val currentUserId = "4U2aXV9OYK5NobTnUEIX"
        db.collection("users").document(currentUserId)
            .collection("friendRequestsReceived")
            .whereEqualTo("status", "pending") // 'pending' 상태인 요청만 가져옴
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
                                    layoutManager = LinearLayoutManager(this@RequestedFriendsActivity)
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
    }
}