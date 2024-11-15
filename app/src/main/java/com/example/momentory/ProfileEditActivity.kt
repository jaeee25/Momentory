package com.example.momentory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ProfileEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // 텍스트 뷰에 "프로필수정페이지<" 문구 표시
        val textView = findViewById<TextView>(R.id.profile_edit_text)
        textView.text = "프로필수정페이지"
    }
}