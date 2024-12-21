package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentSecretDiaryBinding
import com.google.firebase.firestore.FirebaseFirestore

class SecretDiaryFragment : Fragment() {
    private var _binding: FragmentSecretDiaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecretDiaryBinding.inflate(inflater, container, false)


        firestore = FirebaseFirestore.getInstance()


        postAdapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SECRET) { post, position ->
            val intent = Intent(activity, CommentActivity::class.java).apply {
                putExtra("postId", post.id)
                putExtra("postTitle", post.title)
                putExtra("postContent", post.content)
                putExtra("postUser", post.user)
                putExtra("postDate", post.date)
                putExtra("photoUrl", post.photoUrl)
                putExtra("type", "secret")
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = postAdapter

        fetchPostsFromFirestore()


        binding.writeSecretButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun fetchPostsFromFirestore() {
        // Firestore 경로: diary/secret/entries
        firestore.collection("diary")
            .document("secret")
            .collection("entries")
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id
                    postList.add(post)
                }
                postList.sortByDescending { it.date }

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


