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
import com.google.firebase.auth.FirebaseAuth
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

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadUserProfile(currentUserId)

        binding.profileEditBtn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "이름 없음"
                    val phone = document.getString("phoneNumber") ?: "전화번호 없음"
                    val profileImageUrl = document.getString("profileImage") // 프로필 이미지 URL 가져오기
                    binding.profileName.setText(name)
                    binding.profilePhone.setText(phone)
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .override(210, 210)
                            .centerCrop()
                            .into(binding.profileImage)
                    }
                    Log.d("ProfileActivity", "Profile Name : $name, Phone : $phone")
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "정보 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error getting user profile", exception)
            }
    }
}
