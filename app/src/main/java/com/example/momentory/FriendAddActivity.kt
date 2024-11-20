package com.example.momentory

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityFriendsAddBinding

class FriendsAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityFriendsAddBinding by lazy {
            ActivityFriendsAddBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO: phone number format
        binding.friendsAddPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.friendsAddBtn.setOnClickListener() {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}