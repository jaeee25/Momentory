package com.example.momentory

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.momentory.databinding.CustomTabBinding
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val initialFragmentIndex = intent.getIntExtra("fragmentIndex", 0) // 기본값 0
        binding.viewPager.setCurrentItem(initialFragmentIndex, false) // 해당 인덱스로 이동

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->

            val tabBinding = CustomTabBinding.inflate(layoutInflater)


            tabBinding.tabText.text = when (position) {
                0 -> "공유일기"
                1 -> "비밀일기"
                2 -> "타임캡슐"
                else -> ""
            }


            tab.customView = tabBinding.root
        }.attach()

        binding.notification.setOnClickListener {
            val intent = Intent(this, RequestedFriendsActivity::class.java)
            startActivity(intent)
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


    }
}