package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ActivityOpenTimeCapsuleBinding
import com.example.momentory.databinding.ActivityRequestedFriendsBinding
import com.example.momentory.databinding.ItemCapsuleMessageBinding
import com.example.momentory.databinding.ItemCapsuleMessageTestBinding
import com.example.momentory.databinding.ItemTimeCapsuleBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore

data class UnlockTimeCapsule(
    val writerName: String,
    val comment: String,
    val profileImageRes: Int
)

class CapsuleViewHolder(val binding: ItemCapsuleMessageTestBinding) :
    RecyclerView.ViewHolder(binding.root)

class OpenUnlockTimeCapsuleAdapter(private val capsuleList: List<UnlockTimeCapsule>) :
    RecyclerView.Adapter<CapsuleViewHolder>() {
    override fun getItemCount(): Int = capsuleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder =
        CapsuleViewHolder(
            ItemCapsuleMessageTestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
//        holder.binding.capsuleWriterName.text = capsuleList[position].writerName
//        holder.binding.capsuleOpenComment.text = capsuleList[position].comment
//        holder.binding.capsuleWriterProfile.setImageResource(capsuleList[position].profileImageRes)

    }
}

//class OpenLockTimeCapsuleAdapter(private val capsuleList: List<UnlockTimeCapsule>) :
//    RecyclerView.Adapter<CapsuleViewHolder>() {
//    override fun getItemCount(): Int = capsuleList.size
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder =
//        CapsuleViewHolder(
//            ItemCapsuleMessageBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//
//    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
//        holder.binding.capsuleWriterName.text = capsuleList[position].writerName
//        holder.binding.capsuleOpenComment.text = capsuleList[position].comment
//        holder.binding.capsuleWriterProfile.setImageResource(capsuleList[position].profileImageRes)
//    }
//}

class OpenTimeCapsuleActivity : AppCompatActivity() {
    private lateinit var openTimeCapsuleAdapter: OpenUnlockTimeCapsuleAdapter
    private val capsuleList = mutableListOf<UnlockTimeCapsule>()
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityOpenTimeCapsuleBinding by lazy {
            ActivityOpenTimeCapsuleBinding.inflate(layoutInflater)
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val releaseDateMillis = intent.getLongExtra("releaseDate", -1L)
        val createDateMillis = intent.getLongExtra("createDate", -1L)
        val friends = intent.getStringArrayListExtra("friends") ?: emptyList<String>()

//        val releaseDate = if (releaseDateMillis != -1L) Date(releaseDateMillis) else null
//        val createDate = if (createDateMillis != -1L) Date(createDateMillis) else null

        // 전달받은 데이터를 UI에 반영하거나 필요한 작업 수행

        openTimeCapsuleAdapter = OpenUnlockTimeCapsuleAdapter(capsuleList)
        // 예시 데이터 추가
        capsuleList.add(UnlockTimeCapsule("권재희", "메시지 테스트", R.drawable.baseline_person_24))
        capsuleList.add(UnlockTimeCapsule("테스트", "여기에 메시지를 입력하세요", R.drawable.baseline_person_24))
        capsuleList.add(UnlockTimeCapsule("테스트2", "메시지 테스트", R.drawable.baseline_person_24))
        capsuleList.add(UnlockTimeCapsule("테스트3", "여기에 메시지를 입력하세요", R.drawable.baseline_person_24))

        binding.viewTimeCapsule.apply {
//            layoutManager = LinearLayoutManager(this@OpenTimeCapsuleActivity)
            layoutManager = GridLayoutManager(this@OpenTimeCapsuleActivity, 2)
            adapter = OpenUnlockTimeCapsuleAdapter(capsuleList)
        }

        binding.capsuleMessageFriendsList.apply {
            layoutManager = LinearLayoutManager(this@OpenTimeCapsuleActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = FriendsAdapter(friends)
        }

        openTimeCapsuleAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("fragmentIndex", 2) // 1은 두 번째 Fragment의 인덱스
        startActivity(intent)
        finish()
        return true
    }
}