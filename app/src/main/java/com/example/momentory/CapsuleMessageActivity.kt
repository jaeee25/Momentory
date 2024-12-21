package com.example.momentory

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ActivityCapsuleMessageBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

// 타임캡슐 메시지 넣기
class CapsuleMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCapsuleMessageBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapsuleMessageBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.capsuleToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.capsuleToolbar.setNavigationOnClickListener {
            finish()
        }

        val capsuleId = intent.getStringExtra("timeCapsuleId") ?: return
        val friends = intent.getStringArrayListExtra("timeCapsuleFriends") ?: emptyList()
        Log.d("CapsuleMessageActivity", "Friends list: $friends")

        binding.saveMessageButton.setOnClickListener {
            saveTimeCapsuleMessage(capsuleId)
        }

        binding.capsuleMessageFriendsList.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.capsuleMessageFriendsList.adapter = FriendsAdapter(friends)

        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val uri = result.data?.data
                uri?.let {
                    Glide.with(this)
                        .load(it)
                        .override(230, 230)
                        .centerCrop()
                        .into(binding.selectedImage)
                    binding.addImageButton.visibility = View.GONE

                    binding.selectedImage.tag = it
                } ?: run {
                    Log.d("CapsuleMessageActivity", "Image URI is null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.selectedImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }
    }

    private fun saveTimeCapsuleMessage(capsuleId: String) {
        val message = binding.messageEditText.text.toString()
        val imageUri = binding.selectedImage.tag as? Uri

        if (message.isBlank() && imageUri == null) {
            Toast.makeText(this, "메시지와 이미지를 추가해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageToStorage(imageUri) { imageUrl ->
                saveMessageToFirestore(capsuleId, message, imageUrl)
            }
        }
        finish()
    }

    private fun uploadImageToStorage(imageUri: Uri, onSuccess: (String) -> Unit) {
        val fileName = "images/${System.currentTimeMillis()}.jpg" // 고유한 파일 이름 생성
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("CapsuleMessageActivity", "Image URL: $uri")
                    onSuccess(uri.toString()) // 다운로드 URL을 콜백으로 전달
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "이미지 업로드에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMessageToFirestore(capsuleId: String, message: String, imageUrl: String?) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val timeCapsuleData = hashMapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "writer" to currentUserId,
            "status" to "pending",
            "message" to message,
            "imageUri" to (imageUrl ?: "")
        )

        db.collection("timeCapsules").document(capsuleId)
            .collection("messages")
            .add(timeCapsuleData)
            .addOnSuccessListener {
                Toast.makeText(this, "타임 캡슐이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "저장에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
