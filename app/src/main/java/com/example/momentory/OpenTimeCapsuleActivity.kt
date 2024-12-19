package com.example.momentory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityOpenTimeCapsuleBinding
import com.example.momentory.databinding.ItemCapsuleMessageTestBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

// 캡슐 네모 보이는곳
data class UnlockTimeCapsule(
    val id: String,
    var writerName: String,
    val comment: String,
    val capsuleImage: String
)

class CapsuleViewHolder(val binding: ItemCapsuleMessageTestBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(capsule: UnlockTimeCapsule, onItemClick: (UnlockTimeCapsule) -> Unit) {
        Glide.with(binding.root.context)
            .load(capsule.capsuleImage)
            .placeholder(R.drawable.round_send_24)
            .error(R.drawable.ic_comment)
            .centerCrop()
            .into(binding.capsuleImage)

        binding.capsuleLock.visibility = View.GONE
        binding.root.setOnClickListener {
            Log.d("RecyclerView", "Clicked on: ${capsule.writerName}")
            onItemClick(capsule)
        }
    }
}

class OpenUnlockTimeCapsuleAdapter(
    private val capsuleList: List<UnlockTimeCapsule>,
    private val onItemClick: (UnlockTimeCapsule) -> Unit
) : RecyclerView.Adapter<CapsuleViewHolder>() {
    override fun getItemCount(): Int = capsuleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder =
        CapsuleViewHolder(
            ItemCapsuleMessageTestBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        holder.bind(capsuleList[position], onItemClick)
    }
}

class OpenTimeCapsuleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenTimeCapsuleBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var openTimeCapsuleAdapter: OpenUnlockTimeCapsuleAdapter
    private val capsuleList = mutableListOf<UnlockTimeCapsule>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenTimeCapsuleBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val capsuleTitle = intent.getStringExtra("timeCapsuleTitle")
        val friends = intent.getStringArrayListExtra("timeCapsuleFriends")
        Log.d("OpenTimeCapsuleActivity", "Capsule Title: $capsuleTitle")
        Log.d("OpenTimeCapsuleActivity", "Friends list: $friends")
        binding.capsuleMessageTitle.text = capsuleTitle.toString()

        val capsuleId = intent.getStringExtra("timeCapsuleId")
        if (capsuleId != null) {
            db.collection("timeCapsules").document(capsuleId)
                .collection("messages") // capsuleId의 하위 컬렉션
                .get()
                .addOnSuccessListener { result ->
                    val writerIdList = mutableListOf<String>()

                    for (document in result) {
                        Log.d("Firebase", "${document.id} => ${document.data}")
                        val writerId = document.getString("writer") ?: "unknown"
                        val comment = document.getString("message") ?: "메시지가 없습니다."
                        val imageUri = document.getString("imageUri") ?: ""

                        writerIdList.add(writerId)
                        val capsule = UnlockTimeCapsule(
                            id = document.id,
                            writerName = writerId,
                            comment = comment,
                            capsuleImage = imageUri
                        )
                        capsuleList.add(capsule)
                    }

                    val userFetchTasks = writerIdList.map { writerId ->
                        db.collection("users").document(writerId).get()
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(*userFetchTasks.toTypedArray())
                        .addOnSuccessListener { userDocuments ->
                            userDocuments.forEachIndexed { index, documentSnapshot ->
                                val writerName = documentSnapshot.getString("name") ?: "알 수 없는 작성자"
                                capsuleList[index].writerName = writerName
                            }

                            openTimeCapsuleAdapter.notifyDataSetChanged() // 리사이클러뷰 갱신
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebase", "Error getting user names.", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firebase", "Error getting capsule messages.", exception)
                }
        } else {
            Log.e("OpenTimeCapsuleActivity", "No timeCapsuleId found in Intent")
        }

        openTimeCapsuleAdapter = OpenUnlockTimeCapsuleAdapter(capsuleList) { selectedItem ->
            val intent = Intent(this, PopupMessageActivity::class.java).apply {
                putExtra("writerName", selectedItem.writerName)
                putExtra("comment", selectedItem.comment)
                putExtra("capsuleImage", selectedItem.capsuleImage) // 이미지 URI 전달
            }
            startActivity(intent)
        }

        binding.viewTimeCapsule.apply {
            layoutManager = GridLayoutManager(this@OpenTimeCapsuleActivity, 2)
            adapter = openTimeCapsuleAdapter
        }

        binding.wrtieCapsuleMessage.setOnClickListener {
            val intent = Intent(this, CapsuleMessageActivity::class.java).apply {
                putExtra("timeCapsuleId", capsuleId)
                putExtra("timeCapsuleFriends", friends)
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("fragmentIndex", 2) // 1은 두 번째 Fragment의 인덱스
        startActivity(intent)
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_MESSAGE && resultCode == Activity.RESULT_OK) {
            val capsuleId = data?.getStringExtra("timeCapsuleId")
            val capsuleTitle = data?.getStringExtra("timeCapsuleTitle")
            if (capsuleId != null) {
                Log.d("OpenTimeCapsuleActivity", "복원된 타임캡슐 ID: $capsuleId")
                binding.capsuleMessageTitle.text = capsuleTitle ?: "알 수 없는 타임캡슐"
            }
        }
    }

    companion object {
        const val REQUEST_CODE_ADD_MESSAGE = 1001 // 요청 코드
    }
}
