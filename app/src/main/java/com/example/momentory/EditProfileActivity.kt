package com.example.momentory

import android.content.Intent
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
import com.example.momentory.databinding.ActivityEditProfileBinding
import com.example.momentory.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadUserProfile(currentUserId)

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
                        .into(binding.profileNewImage)

                    updateProfileImage(currentUserId, it)
                } ?: run {
                    Log.d("profile", "Image URI is null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.profileNewImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        binding.profileEditBtn.setOnClickListener {
            val newName = binding.profileNewName.text.toString().trim()
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
                    binding.profileNewName.setText(name)
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .override(210, 210)
                            .centerCrop()
                            .into(binding.profileNewImage)
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

        profileImageUrl?.let {
            updates["profileImage"] = it
        }

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                binding.profileNewName.setText(newName)
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

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()

                    updateUserProfile(userId, binding.profileNewName.text.toString(), profileImageUrl)

                    Glide.with(this)
                        .load(profileImageUrl)
                        .override(210, 210)
                        .centerCrop()
                        .into(binding.profileNewImage)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error uploading image", exception)
            }
    }
}