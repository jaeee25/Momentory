package com.example.momentory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityWriteDiaryBinding

class WriteDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteDiaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityWriteDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
