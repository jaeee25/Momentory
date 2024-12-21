package com.example.momentory

import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityPopupMessageBinding

class PopupMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPopupMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityPopupMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setTitle("")
        val writerName = intent.getStringExtra("writerName") ?: "이름 없음"
        val comment = intent.getStringExtra("comment") ?: "메시지가 없습니다."
        val image = intent.getStringExtra("capsuleImage") ?: R.drawable.baseline_photo_24

        binding.capsuleWriterName.text = writerName
        binding.capsuleOpenComment.text = comment

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.baseline_photo_24)
            .error(R.drawable.baseline_stream_24)
            .into(binding.capsuleOpenImage)

        binding.capsuleMessageClose.setOnClickListener {
            finish()
        }
    }
}