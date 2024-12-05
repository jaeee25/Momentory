package com.example.momentory

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.ActivityCommentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var postId: String
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter
    private var profileName: String = "사용자" // 기본값 설정

    // 반응 카운트 변수
    private var smileCount = 0
    private var heartCount = 0
    private var thumbsUpCount = 0
    private var fireCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences에서 프로필 이름 불러오기
        val sharedPref = getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
        profileName = sharedPref.getString("profileName", "사용자") ?: "사용자" // SharedPreferences에서 이름 가져오기

        // Intent로 전달된 데이터 가져오기
        postId = intent.getStringExtra("postId") ?: "default_post_id"
        val postTitle = intent.getStringExtra("postTitle") ?: "제목 없음"
        val postContent = intent.getStringExtra("postContent") ?: "내용 없음"
        val postDate = intent.getStringExtra("postDate") ?: "날짜 없음"
        val postUser = intent.getStringExtra("postUser") ?: "작성자 없음"


        binding.postTitle.text = postTitle
        binding.postContent.setText(postContent)
        binding.postDate.text = postDate
        binding.postUser.text = postUser


        binding.toHome.setOnClickListener { finish() }


        commentAdapter = CommentAdapter(comments)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = commentAdapter

        // Firestore에서 댓글 불러오기
        loadComments()

        // 댓글 추가 버튼 클릭 시 동작
        binding.sendCommentButton.setOnClickListener {
            val newComment = binding.commentEditText.text.toString()
            if (newComment.isNotEmpty()) {
                addComment(newComment)
            } else {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }


        setupReactionButtons()
    }

    private fun loadComments() {
        if (postId == "default_post_id") {
            Toast.makeText(this, "잘못된 게시글 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에서 댓글 가져오기
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                comments.clear()
                for (document in snapshot.documents) {
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글을 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addComment(content: String) {
        if (postId == "default_post_id") {
            Toast.makeText(this, "잘못된 게시글 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에 댓글 추가
        val comment = Comment(author = profileName, content = content, timestamp = System.currentTimeMillis()) // 댓글 작성자를 SharedPreferences에서 가져온 이름으로 설정
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                comments.add(comment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                binding.commentEditText.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글 등록에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupReactionButtons() {
        binding.reactionSmile.setOnClickListener {
            smileCount++
            updateReactions()
        }
        binding.reactionHeart.setOnClickListener {
            heartCount++
            updateReactions()
        }
        binding.reactionThumbsUp.setOnClickListener {
            thumbsUpCount++
            updateReactions()
        }
        binding.reactionFire.setOnClickListener {
            fireCount++
            updateReactions()
        }
    }

    private fun updateReactions() {
        val reactionsText = "😊 $smileCount 😍 $heartCount 👍 $thumbsUpCount 🔥 $fireCount"
        binding.reactions.text = reactionsText
    }
}

data class Comment(
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
