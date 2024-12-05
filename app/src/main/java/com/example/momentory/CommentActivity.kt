package com.example.momentory

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.ActivityCommentBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var postId: String
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter
    private var currentUserName: String = "사용자" // 기본값 설정
    private val currentUserId = "vb6wQZCFD1No8EYwjmQ4" // Firestore에 저장된 사용자 ID
    private var postListener: ListenerRegistration? = null

    // 이모티콘 반응 변수
    private var smileCount = 0
    private var heartCount = 0
    private var thumbsUpCount = 0
    private var fireCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firestore에서 최신 사용자 이름 가져오기
        loadCurrentUserName()

        // 게시글 정보 가져오기
        postId = intent.getStringExtra("postId") ?: "default_post_id"
        val postTitle = intent.getStringExtra("postTitle") ?: "제목 없음"
        val postContent = intent.getStringExtra("postContent") ?: "내용 없음"
        val postDate = intent.getStringExtra("postDate") ?: "날짜 없음"
        val postUser = intent.getStringExtra("postUser") ?: "작성자 없음"

        // 게시글 정보를 UI에 바인딩
        binding.postTitle.text = postTitle
        binding.postContent.setText(postContent)
        binding.postDate.text = postDate
        binding.postUser.text = postUser

        // 리사이클러뷰 설정
        commentAdapter = CommentAdapter(comments)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = commentAdapter

        // 댓글 불러오기
        loadComments()

        // 댓글 작성 버튼
        binding.sendCommentButton.setOnClickListener {
            val newComment = binding.commentEditText.text.toString()
            if (newComment.isNotEmpty()) {
                addComment(newComment)
            } else {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로가기 버튼
        binding.toHome.setOnClickListener {
            finish()
        }

        // 이모티콘 반응 설정
        setupReactionButtons()

        // Firestore에서 리액션 데이터 로드
        loadReactions()
    }

    private fun loadCurrentUserName() {
        firestore.collection("users")
            .document(currentUserId) // Firestore에 저장된 사용자 ID로 사용자 정보 가져오기
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUserName = document.getString("name") ?: "사용자"
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "사용자 이름을 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        if (postId == "default_post_id") {
            Toast.makeText(this, "잘못된 게시글 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 파이어스토어에서 댓글 데이터 불러오기
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

        // 새로운 댓글 추가
        val comment = Comment(author = currentUserName, content = content, timestamp = System.currentTimeMillis())
        val postRef = firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)

        postRef.collection("comments")
            .add(comment)
            .addOnSuccessListener {
                comments.add(comment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                binding.commentEditText.text.clear()

                // 댓글 개수 업데이트
                postRef.collection("comments").get()
                    .addOnSuccessListener { commentSnapshot ->
                        val commentCount = commentSnapshot.size()
                        postRef.update("commentCount", commentCount)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "댓글 개수 업데이트 실패: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글 등록에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupReactionButtons() {
        binding.reactionSmile.setOnClickListener {
            smileCount++
            updateReactions("😊", smileCount)
        }
        binding.reactionHeart.setOnClickListener {
            heartCount++
            updateReactions("😍", heartCount)
        }
        binding.reactionThumbsUp.setOnClickListener {
            thumbsUpCount++
            updateReactions("👍", thumbsUpCount)
        }
        binding.reactionFire.setOnClickListener {
            fireCount++
            updateReactions("🔥", fireCount)
        }
    }

    private fun updateReactions(reactionType: String, newCount: Int) {
        if (postId == "default_post_id") {
            Toast.makeText(this, "잘못된 게시글 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에서 해당 리액션 필드를 업데이트
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .update("reactions.$reactionType", newCount) // "reactions" 필드의 하위 필드 업데이트
            .addOnSuccessListener {

                updateTotalReactions()
                updateReactionUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "리액션 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotalReactions() {
        val totalReactions = smileCount + heartCount + thumbsUpCount + fireCount
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .update("reactionTotal", totalReactions)
            .addOnFailureListener { e ->
                Toast.makeText(this, "전체 리액션 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateReactionUI() {
        val reactionsText = "😊 $smileCount 😍 $heartCount 👍 $thumbsUpCount 🔥 $fireCount"
        binding.reactions.text = reactionsText
    }

    private fun loadReactions() {
        if (postId == "default_post_id") {
            Toast.makeText(this, "잘못된 게시글 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val reactions = document.get("reactions") as? Map<String, Long> ?: emptyMap()
                    smileCount = reactions["😊"]?.toInt() ?: 0
                    heartCount = reactions["😍"]?.toInt() ?: 0
                    thumbsUpCount = reactions["👍"]?.toInt() ?: 0
                    fireCount = reactions["🔥"]?.toInt() ?: 0
                    updateReactionUI()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "리액션 정보를 가져오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

data class Comment(
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
