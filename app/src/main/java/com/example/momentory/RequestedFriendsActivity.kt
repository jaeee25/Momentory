package com.example.momentory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ActivityFriendsAddBinding
import com.example.momentory.databinding.ActivityRequestedFriendsBinding
import com.example.momentory.databinding.FriendsRequestListBinding

class MyViewHolder(val binding: FriendsRequestListBinding) :
    RecyclerView.ViewHolder(binding.root)
class MyAdapter(
    private val names: MutableList<String>,
    private val messages: MutableList<String>
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun getItemCount(): Int = names.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(FriendsRequestListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.requestedFriendName.text = names[position]
        holder.binding.requestedFriendMessage.text = messages[position]
        holder.binding.requestedFriendProfileImage.setImageResource(R.drawable.character)

        holder.binding.friendAcceptBtn.setOnClickListener {
            acceptFriend(position)
            removeItem(position)
        }
        holder.binding.friendRejectBtn.setOnClickListener {
            removeItem(position)
        }
    }

    private fun removeItem(position: Int) {
        names.removeAt(position)
        messages.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    private fun acceptFriend(position: Int) {
        // TODO: 친구 요청 수락 시 친구 목록에 추가
    }
}

class RequestedFriendsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityRequestedFriendsBinding by lazy {
            ActivityRequestedFriendsBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val name = mutableListOf<String>()
        for (i in 1..10) {
            name.add("친구${i}")
        }

        val message = mutableListOf<String>()
        for (i in 1..10) {
            message.add("친구${i}님의 요청 메시지")
        }

        binding.requestedFriendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.requestedFriendsRecyclerView.adapter = MyAdapter(name, message)
    }
}