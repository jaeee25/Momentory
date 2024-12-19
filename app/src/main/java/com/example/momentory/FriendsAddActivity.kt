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

            Log.d("FriendsAddActivity", "전화번호: $phoneNumber, 메시지: $message")
            db.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
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

    // 친구 추가를 요청하는 메서드
    // senderId = currentId
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

                // 3. 친구 요청을 이미 보냈는지 확인
                db.collection("users").document(senderId)
                    .collection("friendRequestsSent").document(receiverId)
                    .get()
                    .addOnSuccessListener { requestDocument ->
                        if (requestDocument.exists()) {
                            Toast.makeText(this, "이미 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            // 4. 친구 요청을 받은 적이 있는지 확인
                            db.collection("users").document(receiverId)
                                .collection("friendRequestsReceived").document(senderId)
                                .get()
                                .addOnSuccessListener { receivedRequestDoc ->
                                    if (receivedRequestDoc.exists()) {
                                        Toast.makeText(this, "이미 친구 요청을 받았습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 친구 요청을 추가할 수 있습니다.
                                        addFriendRequest(senderId, receiverId, message)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Friend", "친구 요청 확인 중 오류 발생", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Friend", "친구 요청을 보냈는지 확인 중 오류 발생", e)
                    }
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

        // 1️⃣ 친구 요청 받는 쪽에 friendRequestsReceived에 추가 (문서 ID = senderId)
        db.collection("users").document(receiverId)
            .collection("friendRequestsReceived")
            .document(senderId) // 🔥 문서 이름을 senderId로 설정
            .set(requestData) // 🔥 set()을 사용하여 덮어쓰기 방지
            .addOnSuccessListener {
                Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        // 2️⃣ 친구 요청 보낸 쪽에 friendRequestsSent에 추가 (문서 ID = receiverId)
        val sentRequestData = requestData.toMutableMap().apply {
            put("toUserId", receiverId)
        }

        db.collection("users").document(senderId)
            .collection("friendRequestsSent")
            .document(receiverId) // 🔥 문서 이름을 receiverId로 설정
            .set(sentRequestData) // 🔥 set()을 사용하여 덮어쓰기 방지
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
