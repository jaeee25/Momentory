package com.example.momentory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class FriendAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_add)

        // 텍스트 뷰에 "친구추가페이지" 문구 표시
        val textView = findViewById<TextView>(R.id.friend_add_text)
        textView.text = "친구추가페이지"
    }
}