package com.example.momentory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.momentory.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val binding = ActivityLoginBinding.inflate(layoutInflater)

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
                binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24) // 잠김 아이콘
            }

            // 커서 위치 유지
            binding.pwLi.setSelection(binding.pwLi.text.length)
        }
        // 처음 시작 시 비밀번호 숨김 상태로 설정
        binding.pwLi.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘


        // (전화번호 + 비밀번호가 맞는 경우 ) -- 구현해야함
        // Log In 버튼 누르면 -> 메인 화면 MainActivity로 이동

        binding.login.setOnClickListener {

                // 로그인 정보 (id, pw) 가져오기
                val id = binding.phoneLi.text.toString()
                val pw = binding.pwLi.text.toString()

                // 로그인 정보 (id, pw) 전달 및 HomeActivity로 이동
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("phone number", id)
                intent.putExtra("password", pw)

                startActivity(intent)

                finish()
        }

        // Forget Password 누르면 -> 비밀번호 찾기 화면 CheckpwActivity로 이동
        binding.forgotPw.setOnClickListener {
            val intent = Intent(this, CheckpwActivity::class.java)
            startActivity(intent)
        }

        // Sign Up 누르면 -> 회원가입 화면 SignUpActivity로 이동
        binding.toSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}