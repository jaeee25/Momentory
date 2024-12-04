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
        val checked = isChecked[position]
        holder.binding.createCapsuleName.text = name

        updateBackgroundColor(holder, checked)
//        val context = holder.itemView.context

        holder.binding.itemRoot.setOnClickListener {
            isChecked[position] = !isChecked[position]
            updateBackgroundColor(holder, isChecked[position])
            onCheckedChange(name, isChecked[position])
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
        val currentUserId = "vb6wQZCFD1No8EYwjmQ4"
        val friendNames = mutableListOf<String>()
        val friendIDs = mutableListOf<String>()
        val isChecked = mutableListOf<Boolean>()
        val selectedFriends = mutableListOf<Pair<String, String>>()  // 친구 이름과 ID를 쌍으로 저장

        // 친구 목록을 Firestore에서 불러오기
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val friends =
                        document.get("friends") as? List<String> ?: emptyList()
                    Log.d("CreateCapsuleActivity", "friends: $friends")
                    friendIDs.clear()
                    friendNames.clear()
                    isChecked.clear()

                    // 각 친구들의 이름을 가져옴
                    friends.forEach { friendId ->
                        db.collection("users").document(friendId)
                            .get()
                            .addOnSuccessListener { friendDoc ->
                                val friendName = friendDoc.getString("name") ?: "이름 없음"
                                val friendID = friendId

                                friendNames.add(friendName)
                                friendIDs.add(friendID)
                                isChecked.add(false)  // 기본적으로 체크되지 않음

                                // RecyclerView 업데이트
                                val adapter =
                                    FriendAdapter(friendNames, isChecked) { name, isCheckedNow ->
                                        if (isCheckedNow) {
                                            // ID와 함께 추가
                                            val friendId = friendIDs[friendNames.indexOf(name)]
                                            selectedFriends.add(Pair(name, friendId))
                                        } else {
                                            // ID와 함께 삭제
                                            val friendId = friendIDs[friendNames.indexOf(name)]
                                            selectedFriends.remove(Pair(name, friendId))
                                        }
                                    }

                                binding.timeCapsuleRecyclerView.layoutManager =
                                    LinearLayoutManager(this@CreateCapsuleActivity)
                                binding.timeCapsuleRecyclerView.adapter = adapter
                                adapter.updateData(friendNames, isChecked)
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
                friendNames
            } else {
                friendNames.filter { it.contains(query) }.toMutableList()
            }
            val filteredChecked = filteredFriends.map { friendNames.indexOf(it) }
                .map { isChecked[it] }
                .toMutableList()

            (binding.timeCapsuleRecyclerView.adapter as FriendAdapter).updateData(
                filteredFriends,
                filteredChecked
            )
        }

        binding.createCapsuleNextBtn.setOnClickListener {
            Log.d("CreateCapsuleActivity", "selectedFriends: $selectedFriends")
            val selectedFriendsList = selectedFriends.map { it.second } // 친구 ID만 사용

            val intent = Intent(this, CreateCapsuleWhenActivity::class.java)
            intent.putStringArrayListExtra("selectedFriends", ArrayList(selectedFriendsList))
            startActivity(intent)
        }
    }
}
