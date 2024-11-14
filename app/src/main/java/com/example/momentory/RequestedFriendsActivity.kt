package com.example.momentory

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.example.momentory.databinding.ActivityRequestedFriendsBinding
import com.example.momentory.databinding.FriendsRequestListBinding

class FriendRequiredActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityRequestedFriendsBinding by lazy {
            ActivityRequestedFriendsBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}

class MyViewHolder(val binding: FriendsRequestListBinding) : RecyclerView.ViewHolder(binding.root)

class MyAdapter(val data: MutableList<String>): RecyclerView.Adapter<MyViewHolder>(){
    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.friendName.text = data[position]
    }

    // issue : 친구 전화번호, 친구 이름 등 -> 백엔드 영역?
}