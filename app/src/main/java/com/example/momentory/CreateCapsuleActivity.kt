package com.example.momentory

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityCreateCapsuleBinding
import com.example.momentory.databinding.ActivityProfileBinding
import com.example.momentory.databinding.TimecapsuleFriendsBinding
import com.google.firebase.firestore.FirebaseFirestore


class FriendViewHolder(val binding: TimecapsuleFriendsBinding) :
    RecyclerView.ViewHolder(binding.root)
class FriendAdapter(
    private val names: MutableList<String>,
    private val isChecked: MutableList<Boolean>,
    private val onCheckedChange: (String, Boolean) -> Unit
) : RecyclerView.Adapter<FriendViewHolder>() {

    override fun getItemCount(): Int = names.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder =
        FriendViewHolder(
            TimecapsuleFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val name = names[position]
        holder.binding.friendName.text = name
        holder.binding.friendCheckBox.isChecked = isChecked[position]

        holder.binding.friendCheckBox.setOnCheckedChangeListener { _, isCheckedNow ->
            isChecked[position] = isCheckedNow
            onCheckedChange(name, isCheckedNow)
        }
    }

    fun updateData(newNames: MutableList<String>, newChecked: MutableList<Boolean>) {
        names.clear()
        names.addAll(newNames)
        isChecked.clear()
        isChecked.addAll(newChecked)
        notifyDataSetChanged()
    }
}

class CreateCapsuleActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityCreateCapsuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCapsuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.capsuleToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 초기화
        val currentUserId = "vb6wQZCFD1No8EYwjmQ4" // 임시 UID, 실제로는 로그인한 유저의 UID를 사용해야 함
        val friends = mutableListOf<String>()
        val isChecked = mutableListOf<Boolean>()
        val selectedFriends = mutableListOf<String>()

        // 친구 목록을 Firestore에서 불러오기
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val friendIds =
                        document.get("friendRequestsReceived") as? List<String> ?: emptyList()
                    friends.clear()
                    isChecked.clear()

                    // 각 친구들의 이름을 가져옴
                    friendIds.forEach { friendId ->
                        db.collection("users").document(friendId)
                            .get()
                            .addOnSuccessListener { friendDoc ->
                                val friendName = friendDoc.getString("name") ?: "Unknown"
                                friends.add(friendName)
                                isChecked.add(false)  // 기본적으로 체크되지 않음

                                // RecyclerView 업데이트
                                val adapter =
                                    FriendAdapter(friends, isChecked) { name, isCheckedNow ->
                                        if (isCheckedNow) {
                                            selectedFriends.add(name)
                                        } else {
                                            selectedFriends.remove(name)
                                        }
                                    }

                                binding.timeCapsuleRecyclerView.layoutManager =
                                    LinearLayoutManager(this@CreateCapsuleActivity)
                                binding.timeCapsuleRecyclerView.adapter = adapter
                                adapter.updateData(friends, isChecked)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "친구 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        binding.searchFriend.setOnClickListener {
            val query = binding.capsuleFriendName.text.toString()
            val filteredFriends = if (query.isEmpty()) {
                friends
            } else {
                friends.filter { it.contains(query) }.toMutableList()
            }
            val filteredChecked = filteredFriends.map { friends.indexOf(it) }
                .map { isChecked[it] }
                .toMutableList()

            (binding.timeCapsuleRecyclerView.adapter as FriendAdapter).updateData(
                filteredFriends,
                filteredChecked
            )
        }

        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val uri = result.data?.data
                uri?.let {
                    Glide.with(this)
                        .load(it)
                        .override(210, 210)
                        .centerCrop()
                        .into(binding.capsuleImage)
                } ?: run {
                    Log.d("profile", "Image URI is null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.capsuleImage.setOnClickListener() {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        binding.createCapsuleNextBtn.setOnClickListener() {
            Log.d("kkang", "selectedFriends: $selectedFriends")
            val intent = Intent(this, CreateCapsuleWhenActivity::class.java)
            startActivity(intent)
        }
    }
}
