package com.example.momentory

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.ActivityCommentBinding

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val comments = mutableListOf<Comment>() // 댓글 리스트
    private lateinit var commentAdapter: CommentAdapter // 댓글 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent로 전달받은 데이터 가져오기
        val postTitle = intent.getStringExtra("postTitle") ?: "제목 없음"
        val postContent = intent.getStringExtra("postContent") ?: "내용 없음"
        val postAuthor = intent.getStringExtra("postAuthor") ?: "작성자 없음"
        val postDate = intent.getStringExtra("postDate") ?: "날짜 없음"
        val postImage = intent.getStringExtra("postImageUrl")

        // 데이터 설정
        binding.postTitle.text = postTitle
        binding.postContent.setText(postContent) // 오류 수정
        binding.postDate.text = postDate
        binding.postAuthor.text = postAuthor

        // 뒤로가기 버튼
        binding.toHome.setOnClickListener {
            finish()
        }

        // 댓글 RecyclerView 설정
        commentAdapter = CommentAdapter(comments)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = commentAdapter

        // 댓글 추가 버튼
        binding.sendCommentButton.setOnClickListener {
            val newComment = binding.commentEditText.text.toString()
            if (newComment.isNotEmpty()) {
                comments.add(Comment("사용자", newComment))
                commentAdapter.notifyItemInserted(comments.size - 1)
                binding.commentEditText.text.clear()
            } else {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class Comment(val author: String, val content: String)
