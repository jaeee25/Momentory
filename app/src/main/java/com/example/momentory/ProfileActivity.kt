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

        val currentUserId = "vb6wQZCFD1No8EYwjmQ4"
        loadUserProfile(currentUserId)
        val beforeName = binding.profileName.text.toString()
        val beforePassword = binding.profilePassword.text.toString()

        binding.profileEditBtn.setOnClickListener {
            val newName = binding.profileName.text.toString().trim()
            val newPassword = binding.profilePassword.text.toString()
            updateUserProfile(currentUserId, newName)

//            if ((newName!=beforeName) or (newPassword!=beforePassword)){
//                updateUserProfile(currentUserId, newName)
//            } else {
//                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
//            }
            finish()
        }
    }


    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "이름 없음"
                    binding.profileName.setText(name)  // EditText에 이름 설정
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "정보 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error getting user profile", exception)
            }
    }

    private fun updateUserProfile(userId: String, newName: String) {
        val updates = mapOf("name" to newName)

        // Firestore에서 사용자 이름을 업데이트
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                binding.profileName.setText(newName)
                Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error updating user name", exception)
            }
    }
}