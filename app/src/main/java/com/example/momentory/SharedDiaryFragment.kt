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
                putExtra("postTitle", post.title)
                putExtra("postContent", post.content)
                putExtra("postUser", post.user)
                putExtra("postDate", post.date)
                putExtra("postImageUrl", post.photoUrl)
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
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching posts", e)
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
