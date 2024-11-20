package com.example.momentory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.profileEditText.text = "프로필수정페이지"
    }
}
