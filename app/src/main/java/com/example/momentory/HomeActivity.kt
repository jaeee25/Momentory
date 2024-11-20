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

        // 툴바 설정 (기본 타이틀 비활성화)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 비활성화

        // ViewPager 설정
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // TabLayout 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // View Binding을 사용해 커스텀 탭 레이아웃 초기화
            val tabBinding = CustomTabBinding.inflate(layoutInflater)

            // 탭 텍스트 설정
            tabBinding.tabText.text = when (position) {
                0 -> "공유일기"
                1 -> "비밀일기"
                2 -> "타임캡슐"
                else -> ""
            }

            // 탭의 커스텀 뷰로 설정
            tab.customView = tabBinding.root
        }.attach()
        // 프로필 이미지 클릭 이벤트
        binding.profileImage.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            startActivity(intent)
        }
    }
}
