package com.example.momentory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore

class MyViewHolder(val binding: FriendsRequestListBinding) :
    RecyclerView.ViewHolder(binding.root)
class MyAdapter(
    private val names: MutableList<String>,
    private val messages: MutableList<String>
) : RecyclerView.Adapter<MyViewHolder>() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
            rejectFriend(position)
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
    }
    private fun rejectFriend(position: Int) {
        // TODO: 친구 요청 거절 시 친구 목록에 추가
    }
}

class RequestedFriendsActivity : AppCompatActivity() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityRequestedFriendsBinding by lazy {
            ActivityRequestedFriendsBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
//
//        db.collection("users").document("vb6wQZCFD1No8EYwjmQ4")
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    Log.d("requestedFriends","DocumentSnapshot data: ${document.data}")
//                }   else{
//                    Log.d("requestedFriends", "No such document")
//                }
//            }
//            .addOnFailureListener{  exception ->
//                Log.d("requestedFriendsDocument", "get failed with", exception)
//            }

        val name = mutableListOf<String>()
        for (i in 1..10) {
            name.add("친구${i}")
        }

        val message = mutableListOf<String>()
        for (i in 1..10) {
            message.add("친구${i}님의 요청 메시지")
        }

        binding.requestedFriendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RequestedFriendsActivity)
            adapter = MyAdapter(name, message)
        }
    }
}