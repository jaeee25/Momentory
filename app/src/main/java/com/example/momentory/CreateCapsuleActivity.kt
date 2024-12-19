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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityCreateCapsuleBinding
import com.example.momentory.databinding.ActivityProfileBinding
import com.example.momentory.databinding.TimecapsuleFriendsBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

data class Friend(
    val id: String,
    val name: String,
    val profileImage: String, // 프로필 이미지 URL 또는 resource
    var isChecked: Boolean = false
)

class FriendViewHolder(val binding: TimecapsuleFriendsBinding) :
    RecyclerView.ViewHolder(binding.root)

class FriendAdapter(
    private val friends: MutableList<Friend>,
    private val onCheckedChange: (Friend, Boolean) -> Unit
) : RecyclerView.Adapter<FriendViewHolder>() {

    override fun getItemCount(): Int = friends.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder =
        FriendViewHolder(
            TimecapsuleFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]

        holder.binding.createCapsuleName.text = friend.name
        holder.binding.createCapsuleProfileImage.setImageResource(R.drawable.baseline_person_24)

        updateBackgroundColor(holder, friend.isChecked)

        holder.binding.itemRoot.setOnClickListener {
            friend.isChecked = !friend.isChecked
            updateBackgroundColor(holder, friend.isChecked)
            onCheckedChange(friend, friend.isChecked)
        }
    }

    private fun updateBackgroundColor(holder: FriendViewHolder, isChecked: Boolean) {
        val context = holder.itemView.context
        val color = if (isChecked) {
            R.color.light_pink
        } else {
            R.color.white
        }
        holder.binding.itemRoot.backgroundTintList =
            ContextCompat.getColorStateList(context, color)
    }

    fun updateData(newFriends: MutableList<Friend>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }
}

class CreateCapsuleActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityCreateCapsuleBinding
    private val selectedFriends = mutableListOf<Friend>() // 선택된 친구들 저장 (클래스 멤버로 정의)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCapsuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.capsuleToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 초기화
        val currentUserId = "vb6wQZCFD1No8EYwjmQ4"
        val friendsList = mutableListOf<Friend>()

        // 친구 목록을 Firestore에서 불러오기
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val friends = document.get("friends") as? List<String> ?: emptyList()
                    Log.d("CreateCapsuleActivity", "friends: $friends")

                    // 각 친구들의 정보를 가져오는 비동기 작업
                    val friendFetchTasks = friends.map { friendId ->
                        db.collection("users").document(friendId).get()
                    }

                    // 모든 친구 데이터를 한 번에 가져오기
                    Tasks.whenAllSuccess<DocumentSnapshot>(*friendFetchTasks.toTypedArray())
                        .addOnSuccessListener { friendDocuments ->
                            friendDocuments.forEachIndexed { index, friendDoc ->
                                val friendName = friendDoc.getString("name") ?: "이름 없음"
                                val profileImage = friendDoc.getString("profileImage") ?: "default_image"
                                val friendID = friends[index]

                                // Friend 객체 생성
                                val friend = Friend(
                                    id = friendID,
                                    name = friendName,
                                    profileImage = profileImage,
                                    isChecked = false
                                )

                                // 친구 리스트에 추가
                                friendsList.add(friend)
                            }

                            // 모든 친구 데이터를 불러온 후 RecyclerView 및 Adapter 설정
                            setupRecyclerView(friendsList)

                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "친구 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "친구 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        // 친구 검색
        binding.searchFriend.setOnClickListener {
            val query = binding.capsuleFriendName.text.toString()
            val filteredFriends = if (query.isEmpty()) {
                friendsList
            } else {
                friendsList.filter { it.name.contains(query) }.toMutableList()
            }

            // RecyclerView 갱신
            (binding.timeCapsuleRecyclerView.adapter as FriendAdapter).updateData(
                filteredFriends
            )
        }

        // 다음 단계로 이동
        binding.createCapsuleNextBtn.setOnClickListener {
            Log.d("CreateCapsuleActivity", "selectedFriends: $selectedFriends")
            val selectedFriendsList = selectedFriends.map { it.id } // 친구 ID만 사용

            val intent = Intent(this, CreateCapsuleWhenActivity::class.java)
            intent.putStringArrayListExtra("selectedFriends", ArrayList(selectedFriendsList))
            startActivity(intent)
        }
    }

    // RecyclerView 설정하는 함수
    private fun setupRecyclerView(friendsList: MutableList<Friend>) {
        binding.timeCapsuleRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateCapsuleActivity)
            adapter = FriendAdapter(friendsList) { friend, isCheckedNow ->
                if (isCheckedNow) {
                    selectedFriends.add(friend)
                } else {
                    selectedFriends.remove(friend)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("fragmentIndex", 2)
        startActivity(intent)
        finish()
        return true
    }
}
