package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar?.title = "눈송이의 일기장" // 또는 null로 유지하고 XML 설정을 사용해도 됩니다.

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)


        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "공유일기"
                1 -> "비밀일기"
                2 -> "타임캡슐"
                else -> null
            }
        }.attach()



        val profileImageView = findViewById<ImageView>(R.id.profile_image)
        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            startActivity(intent)
        }


    }

}
