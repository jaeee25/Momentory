package com.example.momentory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityOpenTimeCapsuleBinding
import com.example.momentory.databinding.ItemCapsuleMessageTestBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 캡슐 네모 보이는곳

data class UnlockTimeCapsule(
    val id: String,
    val writerId: String,
    var writerName: String,
    val comment: String,
    val capsuleImage: String
)

class CapsuleViewHolder(val binding: ItemCapsuleMessageTestBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(capsule: UnlockTimeCapsule, onItemClick: (UnlockTimeCapsule) -> Unit) {
        if (capsule.capsuleImage.isNullOrEmpty()) {
            binding.capsuleImage.setImageResource(R.drawable.ic_sample_image) // 기본 이미지로 설정
        } else {
            Glide.with(binding.root.context)
                .load(capsule.capsuleImage)
                .placeholder(R.drawable.round_send_24)
                .error(R.drawable.ic_comment)
                .centerCrop()
                .into(binding.capsuleImage)
        }

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

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val capsuleTitle = intent.getStringExtra("timeCapsuleTitle") //2024년 12월 21일
        val friends = intent.getStringArrayListExtra("timeCapsuleFriends")
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val capsuleDate: Date = dateFormat.parse(capsuleTitle)

        binding.capsuleMessageTitle.text = capsuleTitle.toString()

        val capsuleId = intent.getStringExtra("timeCapsuleId")
        if (capsuleId != null) {
            db.collection("timeCapsules").document(capsuleId)
                .collection("messages")
                .get()
                .addOnSuccessListener { result ->
                    val writerIdList = mutableListOf<String>()

                    for (document in result) {
                        Log.d("Firebase", "${document.id} => ${document.data}")
                        val writerId = document.getString("writer") ?: "unknown"
                        val comment = document.getString("message") ?: "메시지가 없습니다."
                        var imageUri = document.getString("imageUri") ?: ""

                        if(capsuleDate.after(Date()) && writerId != currentUserId) {
                            imageUri = ""
                        }

                        writerIdList.add(writerId)
                        val capsule = UnlockTimeCapsule(
                            id = document.id,
                            writerId = writerId,
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

                            openTimeCapsuleAdapter.notifyDataSetChanged()
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
            if (capsuleDate != null && capsuleDate.after(Date()) && selectedItem.writerId != currentUserId) {
                Toast.makeText(this, "열 수 없는 타임캡슐입니다.", Toast.LENGTH_SHORT).show()
                val animation = AnimationUtils.loadAnimation(this, R.anim.shake) // 흔들리는 애니메이션 적용
                binding.viewTimeCapsule.startAnimation(animation)
            } else {
                val intent = Intent(this, PopupMessageActivity::class.java).apply {
                    putExtra("writerName", selectedItem.writerName)
                    putExtra("comment", selectedItem.comment)
                    putExtra("capsuleImage", selectedItem.capsuleImage)
                }
                startActivity(intent)
            }
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
        intent.putExtra("fragmentIndex", 2)
        startActivity(intent)
        finish()
        return true
    }

}
