package com.example.momentory

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.momentory.databinding.ActivityCheckpwBinding

class CheckpwActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val binding = ActivityCheckpwBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 사용자가 입력한 전화번호 - 데이터베이스에서 확인 후 일치하면 -> 인증번호 발송

        // 인증 번호가 일치하면 인증이 완료되었다는 메세지와 함께
        // 비밀번호를 변경할 수 있는 칸이 보여짐

        // 일치하지 않는다면 일치하는 전화번호가 없다 & 회원가입을 진행해달라는 메시지 출력

        // 새로운 비밀번호가 입력되면 데이터베이스 갱신 (수정 및 저장) 및 변경이 완료되었다는 메세지 출력
        binding.changePw.setOnClickListener {

            // 새로운 비밀번호 입력
            val newPw = binding.pwNew.text.toString()

            Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
        }
        // 타이틀 옆 이전 버튼 클릭하면 로그인 화면으로 이동
        binding.toLogin.setOnClickListener {
            finish()
        }
    }
}