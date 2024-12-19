package com.example.momentory

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityButtonBinding
import com.example.momentory.databinding.ActivityProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
class ProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val currentUserId = "vb6wQZCFD1No8EYwjmQ4" // 현재 사용자 ID (FirebaseAuth로 교체 가능)

        // 갤러리에서 이미지 선택
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val uri = result.data?.data
                uri?.let {
                    Glide.with(this)
                        .load(it)
                        .override(210, 210)
                        .centerCrop()
                        .into(binding.profileImage)

                    // 선택된 이미지를 Firestore에 업로드하도록 요청
                    updateProfileImage(currentUserId, it)
                } ?: run {
                    Log.d("profile", "Image URI is null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        // 사용자 프로필 로드
        loadUserProfile(currentUserId)

        // 프로필 수정 버튼 클릭
        binding.profileEditBtn.setOnClickListener {
            val newName = binding.profileName.text.toString().trim()
            updateUserProfile(currentUserId, newName)
            finish()
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "이름 없음"
                    val profileImageUrl = document.getString("profileImage") // 프로필 이미지 URL 가져오기
                    binding.profileName.setText(name)
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .override(210, 210)
                            .centerCrop()
                            .into(binding.profileImage)
                    }
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "정보 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error getting user profile", exception)
            }
    }

    private fun updateUserProfile(userId: String, newName: String, profileImageUrl: String? = null) {
        val updates = mutableMapOf<String, Any>()
        updates["name"] = newName

        // 이미지 URL이 있으면 업데이트에 추가
        profileImageUrl?.let {
            updates["profileImage"] = it
        }

        // Firestore에서 사용자 이름 및 이미지 URL 업데이트
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                binding.profileName.setText(newName)
                Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error updating user profile", exception)
            }
    }

    private fun updateProfileImage(userId: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profileImageRef = storageRef.child("profile_images/${userId}.jpg")

        // 이미지 업로드
        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                // 업로드 성공 후, 다운로드 URL을 가져와서 Firestore에 저장
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()

                    // Firestore에 이미지 URL 저장
                    updateUserProfile(userId, binding.profileName.text.toString(), profileImageUrl)

                    // 이미지가 저장된 후, 프로필 이미지 갱신
                    Glide.with(this)
                        .load(profileImageUrl)
                        .override(210, 210)
                        .centerCrop()
                        .into(binding.profileImage)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error uploading image", exception)
            }
    }
}
