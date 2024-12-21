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
import com.example.momentory.databinding.ActivityProfileBinding
import com.example.momentory.databinding.ActivitySingnUpProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SingnUpProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivitySingnUpProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingnUpProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
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
                        .into(binding.signUpProfileImage)

                    updateProfileImage(currentUserId, it)
                } ?: run {
                    Log.d("profile", "Image URI is null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.signUpProfileImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        binding.signUpProfileBtn.setOnClickListener {
            val signName = binding.signUpProfileName.text.toString().trim()
            if(signName.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
                updateUserProfile(currentUserId, signName)
                finish()
            }
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
                Toast.makeText(this, "프로필이 설정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "프로필 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("SingUpProfileActivity", "Error setting user profile", exception)
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
                    updateUserProfile(userId, binding.signUpProfileName.text.toString(), profileImageUrl)

                    // 이미지가 저장된 후, 프로필 이미지 갱신
                    Glide.with(this)
                        .load(profileImageUrl)
                        .override(210, 210)
                        .centerCrop()
                        .into(binding.signUpProfileImage)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error uploading image", exception)
            }
    }
}