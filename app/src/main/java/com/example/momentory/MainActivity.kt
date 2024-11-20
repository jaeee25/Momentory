package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.momentory.databinding.ActivityFriendAddBinding
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.example.momentory.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityMainBinding by lazy {
            ActivityMainBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        GlobalScope.launch {
            delay(3000)
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}