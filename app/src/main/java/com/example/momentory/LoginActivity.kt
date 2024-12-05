package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityLoginBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 비밀번호 입력 칸에 있는 자물쇠 아이콘 누르면 아이콘이 변경 및 비밀번호 숨김 / 표시
        binding.seepwLi.setOnClickListener {
            if (binding.pwLi.transformationMethod is PasswordTransformationMethod) {
                // 현재 숨김 상태 => 보이기 상태로 전환
                binding.pwLi.transformationMethod = null
                binding.seepwLi.setImageResource(R.drawable.baseline_lock_open_24) // 잠금이 풀린 아이콘
            } else {
                // 현재 보이기 상태 => 숨김 상태로 전환
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
            val id = binding.phoneLi.text.toString()
            val pw = binding.pwLi.text.toString()

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에서 데이터 확인
            checkLogin(id, pw)
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
    }

    private fun checkLogin(id: String, pw: String) {
        db.collection("users")
            .document("signUp")
            .collection("entries")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedPw = document.getString("pw")
                    if (savedPw == pw) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        // HomeActivity로 이동 및 사용자 정보 전달
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("phoneNumber", id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "존재하지 않는 사용자입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
