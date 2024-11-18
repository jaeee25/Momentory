package com.example.momentory

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.momentory.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val binding = ActivitySignupBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 비밀번호 입력 칸에 있는 자물쇠 아이콘 누르면 아이콘이 변경 및 비밀번호 숨김 / 표시
        binding.seepwSu.setOnClickListener {
            if (binding.pwSu.transformationMethod is PasswordTransformationMethod) {
                // 현재 숨김 상태 => 보이기 상태로 전환
                binding.pwSu.transformationMethod = null
                binding.seepwSu.setImageResource(R.drawable.baseline_lock_open_24) // 잠금이 풀린 아이콘
            } else {
                // 현재 보이기 상태 => 숨김 상태로 전환
                binding.pwSu.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.seepwSu.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘
            }

            // 커서 위치 유지
            binding.pwSu.setSelection(binding.pwSu.text.length)
        }

// 처음 시작 시 비밀번호 숨김 상태로 설정
        binding.pwSu.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwSu.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘


        // Sign Up 버튼 누르면 -> 회원가입 정보 데이터베이스에 저장
        // 아이디(전화번호) & 비밀번호
        binding.signup.setOnClickListener {

            // 회원가입 정보 (id, pw) 가져오기
            val id = binding.phoneSu.text.toString()
            val pw = binding.pwSu.text.toString()

            // 회원가입 정보 (id, pw) 전달
            // 데이터베이스에 저장

            Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        }




        // Kakao Talk으로 회원가입 기능

        // 타이틀 옆 이전 버튼 클릭하면 로그인 화면으로 이동
        binding.toLogin.setOnClickListener {
            finish()
        }

    }
}