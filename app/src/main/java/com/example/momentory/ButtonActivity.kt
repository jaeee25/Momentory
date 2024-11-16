package com.example.momentory
//t

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityButtonBinding

class ButtonActivity : AppCompatActivity() {
    val binding: ActivityButtonBinding by lazy {
        ActivityButtonBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.friendBtn.setOnClickListener(){
            val intent = Intent(this, FriendAddActivity::class.java)
            startActivity(intent)
        }

        binding.profileBtn.setOnClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.requestedFriendsBtn.setOnClickListener(){
            val intent = Intent(this, RequestedFriendsActivity::class.java)
            startActivity(intent)
        }
    }


}