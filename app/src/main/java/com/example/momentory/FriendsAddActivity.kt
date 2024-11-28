package com.example.momentory

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsAddActivity : AppCompatActivity() {
    var db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityFriendsAddBinding by lazy {
            ActivityFriendsAddBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO: phone number format
        binding.friendsAddPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.friendsAddBtn.setOnClickListener() {
            val phoneNumber = binding.friendsAddPhone.text.toString().trim()
            db.collection("users")
                .whereEqualTo("phone", phoneNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val userId = document.id
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            currentUserId?.let {
                                sendFriendRequest(it, userId)
                            }
                        }
                    } else {
                        Toast.makeText(this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendFriendRequest(senderId: String, receiverId: String) {
        val requestMap = mapOf(
            "friendRequestsReceived" to FieldValue.arrayUnion(senderId)
        )

        // 친구 요청을 받는 사용자의 friendRequestsReceived 필드에 추가
        db.collection("users").document(receiverId)
            .update(requestMap)
            .addOnSuccessListener {
                Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        // 보낸 사용자의 friendRequestsSent 필드에도 추가
        db.collection("users").document(senderId)
            .update("friendRequestsSent", FieldValue.arrayUnion(receiverId))
            .addOnSuccessListener {
                Log.d("FriendsAddActivity", "요청을 보낸 목록에 추가 완료")
            }
            .addOnFailureListener {
                Log.e("FriendsAddActivity", "요청 보낸 목록 업데이트 실패", it)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}