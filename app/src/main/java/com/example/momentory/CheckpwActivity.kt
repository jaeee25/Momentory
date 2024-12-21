package com.example.momentory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityCheckpwBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class CheckpwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckpwBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckpwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // 비밀번호 재설정 이메일 전송 버튼
        binding.sendMsg.setOnClickListener {
            val email = binding.phoneCert.text.toString().trim()

            if (email.isNotEmpty() && isValidEmail(email)) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "올바른 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 이전 버튼 클릭 시 로그인 화면으로 이동
        binding.toLogin.setOnClickListener {
            finish()
        }
        binding.back.setOnClickListener {
            finish()
        }
    }

    // 비밀번호 재설정 이메일 전송
    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "비밀번호 재설정 이메일이 전송되었습니다. 이메일을 확인해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "이메일 전송에 실패했습니다."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 이메일 형식 검사 함수
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }
}
