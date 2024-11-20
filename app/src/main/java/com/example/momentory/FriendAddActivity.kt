package com.example.momentory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityFriendAddBinding

class FriendAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityFriendAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.friendAddText.text = "친구추가페이지"
    }
}
