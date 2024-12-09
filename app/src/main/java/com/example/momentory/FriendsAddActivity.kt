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
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityFriendsAddBinding by lazy {
            ActivityFriendsAddBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.friendsAddPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.friendsAddBtn.setOnClickListener {
            val rawPhoneNumber = binding.friendsAddPhone.text.toString().trim()
            val phoneNumber = rawPhoneNumber.replace(Regex("[^0-9]"), "")
            val message = binding.friendsMessage.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (message.isEmpty()) {
                Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("phone", phoneNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val receiverId = document.id
                            // val senderId = FirebaseAuth.getInstance().currentUser?.uid
                            val senderId = "vb6wQZCFD1No8EYwjmQ4" // 임시 UID
                            sendFriendRequest(senderId, receiverId, message)
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendFriendRequest(senderId: String, receiverId: String, message: String) {
        val userRef = db.collection("users").document(senderId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // 1. 자신의 ID인지 확인
                if (receiverId == senderId) {
                    Toast.makeText(this, "자신을 친구로 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 2. 이미 친구 목록에 있는지 확인
                val friends = document.get("friends") as? List<String> ?: emptyList()
                if (friends.contains(receiverId)) {
                    Toast.makeText(this, "이미 친구입니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 3. 이미 친구 요청을 보낸 상대인지 확인
                val friendRequestsSent =
                    document.get("friendRequestsSent") as? List<String> ?: emptyList()
                if (friendRequestsSent.contains(receiverId)) {
                    Toast.makeText(this, "이미 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 4. 친구 요청을 이미 받은 상대인지 확인
                val friendRequestsReceived =
                    document.get("friendRequestsReceived") as? List<String> ?: emptyList()
                if (friendRequestsReceived.contains(receiverId)) {
                    Toast.makeText(this, "이미 친구 요청을 받았습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                // 친구 추가 가능
                addFriendRequest(senderId, receiverId, message)
            } else {
                Log.e("Friend", "User document does not exist.")
            }
        }.addOnFailureListener { e ->
            Log.e("Friend", "Error fetching user data", e)
        }
    }

    private fun addFriendRequest(senderId: String, receiverId: String, message: String) {
        // 요청 정보 생성
        val requestData = mapOf(
            "fromUserId" to senderId,
            "message" to message,
            "status" to "pending"
        )

        // 친구 요청 받는 쪽에 friendRequestsReceived에 추가
        db.collection("users").document(receiverId)
            .collection("friendRequestsReceived")
            .add(requestData)
            .addOnSuccessListener {
                Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        // 친구 요청 보낸 쪽에 friendRequestsSent에 추가
        val sentRequestData = requestData.toMutableMap().apply {
            put("toUserId", receiverId)
        }

        db.collection("users").document(senderId)
            .collection("friendRequestsSent")
            .add(sentRequestData)
            .addOnSuccessListener {
                Log.d("FriendsAddActivity", "친구 요청 보낸 목록에 추가 완료")
            }
            .addOnFailureListener {
                Log.e("FriendsAddActivity", "친구 요청 보낸 목록 업데이트 실패", it)
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
