package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.momentory.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityLoginBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 비밀번호 입력 칸에 있는 자물쇠 아이콘 누르면 아이콘 변경 및 비밀번호 숨김 / 표시
        binding.seepwLi.setOnClickListener {
            if (binding.pwLi.transformationMethod is PasswordTransformationMethod) {
                // 현재 숨김 상태 -> 보이기 상태로 전환
                binding.pwLi.transformationMethod = null
                binding.seepwLi.setImageResource(R.drawable.baseline_lock_open_24) // 잠금이 풀린 아이콘
            } else {
                // 현재 보이기 상태 -> 숨김 상태로 전환
                binding.pwLi.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘
            }

            // 커서 위치 유지
            binding.pwLi.setSelection(binding.pwLi.text.length)
        }

        // 처음 시작 시 비밀번호 숨김 상태로 설정
        binding.pwLi.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘

        // Log In 버튼 클릭 이벤트
        binding.login.setOnClickListener {
            val phoneNumber = binding.phoneLi.text.toString().trim()
            val password = binding.pwLi.text.toString().trim()

            if (phoneNumber.isNotEmpty() && password.isNotEmpty()) {
                loginUser(phoneNumber, password)
            } else {
                Toast.makeText(this, "전화번호와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // Forget Password 클릭 이벤트
        binding.forgotPw.setOnClickListener {
            val intent = Intent(this, CheckpwActivity::class.java)
            startActivity(intent)
        }

        // Sign Up 클릭 이벤트
        binding.toSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // "Google 로그인" 클릭 시 (미구현)
        binding.googleLi.setOnClickListener {
        }
    }

    // 사용자 로그인 처리
    private fun loginUser(phoneNumber: String, password: String) {
        FirebaseFirestore.getInstance().collection("user").document(phoneNumber)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val storedPassword = document.getString("password")
                    if (storedPassword == password) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "등록된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "로그인 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

}
