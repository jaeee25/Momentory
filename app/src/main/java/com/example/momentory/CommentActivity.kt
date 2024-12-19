package com.example.momentory

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ActivityCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var postId: String
    private lateinit var type: String // 공유인지 비공개인지 구분
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter

    private var currentUserName: String = "눈송이" // 기본값 설정
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Firestore에 저장된 사용자 ID

    // 이모티콘 반응 변수
    private var smileCount = 0
    private var heartCount = 0
    private var thumbsUpCount = 0
    private var fireCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firestore에서 사용자 이름 가져오기
        loadCurrentUserName()

        // 전달받은 게시글 ID와 타입 (공개/비공개)
        postId = intent.getStringExtra("postId") ?: "default_post_id"
        type = intent.getStringExtra("type") ?: "share"

        // UI에 게시글 데이터 표시 & 최신화
        loadPostData()

        // 댓글 리사이클러뷰 설정
        commentAdapter = CommentAdapter(comments)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = commentAdapter

        // 댓글 및 리액션 데이터 불러오기
        loadComments()
        loadReactions()

        // 댓글 추가 이벤트
        binding.sendCommentButton.setOnClickListener {
            val newComment = binding.commentEditText.text.toString()
            if (newComment.isNotEmpty()) addComment(newComment)
            else Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
        }

        // 리액션 버튼 설정
        setupReactionButtons()

        // 뒤로가기 버튼
        binding.toHome.setOnClickListener { finish() }
    }

    /**
     * Firestore에서 게시글 데이터를 가져와 UI에 반영
     */
    private fun loadPostData() {
        val postRef = firestore.collection("diary")
            .document(type) // 공유인지 비공개인지 동적으로 설정
            .collection("entries")
            .document(postId)

        postRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val postTitle = document.getString("title") ?: "제목 없음"
                    val postContent = document.getString("content") ?: "내용 없음"
                    val postDate = document.getString("date") ?: "날짜 없음"
                    val postUser = document.getString("user") ?: "작성자 없음"
                    val postPhotoUrl = document.getString("photoUrl") ?: ""

                    // UI에 최신 데이터 반영
                    binding.postTitle.text = postTitle
                    binding.postContent.text = postContent
                    binding.postDate.text = postDate
                    binding.postUser.text = postUser

                    Glide.with(this)
                        .load(postPhotoUrl)
                        .placeholder(R.drawable.ic_sample_image)
                        .error(R.drawable.ic_error_image)
                        .into(binding.postPhoto)

                } else {
                    Toast.makeText(this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에서 사용자 이름 불러오기
    private fun loadCurrentUserName() {
        currentUserId?.let { userId ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    currentUserName = document.getString("name") ?: "알 수 없는 작성자"
                }
                .addOnFailureListener {
                    currentUserName = "알 수 없는 작성자"
                }
        }
    }


    // 댓글 불러오기
    private fun loadComments() {
        firestore.collection("diary").document("share")
            .collection("entries").document(postId)
            .collection("comments").orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "댓글 불러오기 실패: ${e.message}")
                    return@addSnapshotListener
                }

                comments.clear()
                snapshot?.documents?.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

    // 댓글 추가하기
    private fun addComment(content: String) {
        val comment = Comment(
            author = currentUserName, // 현재 사용자 이름 저장
            content = content,
            timestamp = System.currentTimeMillis()
        )

        val postRef = firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)

        postRef.collection("comments").add(comment)
            .addOnSuccessListener {
                binding.commentEditText.text.clear()

                // 댓글 개수 업데이트
                postRef.collection("comments").get().addOnSuccessListener { snapshot ->
                    val commentCount = snapshot.size()
                    postRef.update("commentCount", commentCount)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 리액션 버튼 설정
    private fun setupReactionButtons() {
        binding.reactionSmile.setOnClickListener { updateReactions("😊") }
        binding.reactionHeart.setOnClickListener { updateReactions("😍") }
        binding.reactionThumbsUp.setOnClickListener { updateReactions("👍") }
        binding.reactionFire.setOnClickListener { updateReactions("🔥") }
    }

    // 리액션 업데이트
    private fun updateReactions(reactionType: String) {
        val postRef = firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)

        postRef.update(
            mapOf(
                "reactions.$reactionType" to FieldValue.increment(1),
                "reactionTotal" to FieldValue.increment(1)
            )
        ).addOnSuccessListener { loadReactions() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "리액션 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 리액션 불러오기
    private fun loadReactions() {
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val reactions = document.data?.get("reactions") as? Map<String, Long> ?: mapOf()
                    Log.d("Reactions", "Reactions loaded: $reactions") // 디버깅용 로그

                    smileCount = reactions["😊"]?.toInt() ?: 0
                    heartCount = reactions["😍"]?.toInt() ?: 0
                    thumbsUpCount = reactions["👍"]?.toInt() ?: 0
                    fireCount = reactions["🔥"]?.toInt() ?: 0

                    updateReactionUI()
                } else {
                    Log.d("Reactions", "Document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Reactions", "Failed to load reactions: ${e.message}")
                Toast.makeText(this, "리액션 정보를 가져오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // 리액션 UI 업데이트
    private fun updateReactionUI() {
        val reactionText = "😊 $smileCount 😍 $heartCount 👍 $thumbsUpCount 🔥 $fireCount"
        binding.reactions.text = reactionText
    }
}

// 댓글 데이터 클래스
data class Comment(
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)



