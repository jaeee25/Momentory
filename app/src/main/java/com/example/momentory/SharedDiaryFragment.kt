package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentSharedDiaryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SharedDiaryFragment : Fragment() {
    private var _binding: FragmentSharedDiaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharedDiaryBinding.inflate(inflater, container, false)

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()

        // RecyclerView 설정
        postAdapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SHARED) { post, position ->
            // 게시글 클릭 이벤트 (CommentActivity로 이동)
            val intent = Intent(activity, CommentActivity::class.java).apply {
            putExtra("postId", post.id) // 여기서 post.id가 Firestore 문서 ID여야 함
            putExtra("postTitle", post.title)
            putExtra("postContent", post.content)
            putExtra("postUser", post.user)
            putExtra("postDate", post.date)
        }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = postAdapter

        // Firestore에서 데이터 가져오기
        fetchPostsFromFirestore()

        // 친구 추가 버튼 클릭 이벤트
        binding.addFriendButton.setOnClickListener {
            val intent = Intent(activity, FriendsAddActivity::class.java)
            startActivity(intent)
        }

        // 글 작성 버튼 클릭 이벤트
        binding.writeDiaryButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun fetchPostsFromFirestore() {
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id // 문서 ID를 Post 객체에 저장
                    val postRef = document.reference

                    // 댓글 수
                    postRef.collection("comments").get()
                        .addOnSuccessListener { commentsSnapshot ->
                            post.commentCount = commentsSnapshot.size()

                            // 리액션 합계
                            postRef.get()
                                .addOnSuccessListener { postSnapshot ->
                                    val reactions = postSnapshot.get("reactions") as? Map<String, Long>
                                    post.reactionTotal = reactions?.values?.sum()?.toInt() ?: 0

                                    postList.add(post)

                                    // 데이터가 변경되었음을 알림
                                    postList.sortByDescending { it.date } // 날짜 필드 기준 내림차순 정렬
                                    postAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "리액션 합계 가져오기 실패: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "댓글 수 가져오기 실패: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "게시글 가져오기 실패: ${e.message}")
            }
    }



    override fun onResume() {
        super.onResume()
        fetchPostsFromFirestore()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}