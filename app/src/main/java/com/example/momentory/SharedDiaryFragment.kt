package com.example.momentory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentSharedDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SharedDiaryFragment : Fragment() {
    private var _binding: FragmentSharedDiaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    private lateinit var friendsAdapter: FriendsProfileAdapter
    private val friendsUidList = mutableListOf<String>() // UID 리스트

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharedDiaryBinding.inflate(inflater, container, false)

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()

        // RecyclerView 설정
        postAdapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SHARED) { post, position ->
            val intent = Intent(activity, CommentActivity::class.java).apply {
                putExtra("postId", post.id)
                putExtra("postTitle", post.title)
                putExtra("postContent", post.content)
                putExtra("postUser", post.user)
                putExtra("postDate", post.date)
                putExtra("postImageUrl", post.imageUrl)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = postAdapter

        // 친구 프로필 RecyclerView 설정
        friendsAdapter = FriendsProfileAdapter(emptyList())
        binding.friendsProfileList.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.friendsProfileList.adapter = friendsAdapter

        // Firestore에서 데이터 가져오기
        fetchPostsFromFirestore()
        fetchFriendsUids()
        loadUserName() // 사용자 이름 가져오기

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

    override fun onResume() {
        super.onResume()
        loadUserName() // 프래그먼트로 돌아올 때 이름 업데이트
    }


    private fun loadUserName() {
        val sharedPref = requireActivity().getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("profileName", null)
        Log.d("FirestoreDiaryName", "저장된 이름: $savedName")

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title) // 직접 참조

        if (!savedName.isNullOrBlank()) {
            // SharedPreferences에 저장된 이름 사용
            Log.d("FirestoreDiaryName", "저장된 이름 사용: $savedName")
            toolbarTitle.text = "${savedName} 일기장"
        } else {
            // Firestore에서 사용자 이름 가져오기
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            firestore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "눈송이"
                        Log.d("FirestoreDiaryName", "Firestore에서 이름 가져옴: $userName")

                        // UI 업데이트
                        toolbarTitle.text = "${userName} 일기장"

                        // SharedPreferences에 저장
                        val editor = sharedPref.edit()
                        editor.putString("profileName", userName)
                        editor.apply()
                    } else {
                        Log.d("FirestoreDiaryName", "Firestore에 사용자 정보 없음. 기본값 설정")
                        val defaultName = "눈송이"
                        toolbarTitle.text = "${defaultName} 일기장"


                        val editor = sharedPref.edit()
                        editor.putString("profileName", defaultName)
                        editor.apply()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "사용자 이름 가져오기 실패", e)
                    toolbarTitle.text = "눈송이 일기장"
                }
        }
    }



    private fun fetchPostsFromFirestore() {
        firestore.collection("diary")
            .document("share")
            .collection("entries")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                val tempPostList = mutableListOf<Post>()

                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    post.id = document.id
                    val postRef = document.reference

                    // 댓글 수 가져오기
                    postRef.collection("comments").get()
                        .addOnSuccessListener { commentsSnapshot ->
                            post.commentCount = commentsSnapshot.size()

                            // 리액션 합계 가져오기
                            postRef.get()
                                .addOnSuccessListener { postSnapshot ->
                                    val reactions =
                                        postSnapshot.get("reactions") as? Map<String, Long>
                                    post.reactionTotal = reactions?.values?.sum()?.toInt() ?: 0

                                    tempPostList.add(post)

                                    if (tempPostList.size == documents.size()) {
                                        postList.clear()
                                        postList.addAll(tempPostList.sortedByDescending { it.date })
                                        postAdapter.notifyDataSetChanged()
                                    }
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "게시글 가져오기 실패: ${e.message}")
            }
    }

    private fun fetchFriendsUids() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val friends = document.get("friends") as? List<String> ?: emptyList()
                friendsUidList.clear()
                friendsUidList.addAll(friends)

                fetchFriendsProfiles(friendsUidList) // 친구 프로필 정보 가져오기
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "친구 목록 가져오기 실패", e)
            }
    }

    private fun fetchFriendsProfiles(friendsUidList: List<String>) {
        val friendsProfiles = mutableListOf<FriendProfile>()

        for (uid in friendsUidList) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    friendsProfiles.add(FriendProfile(uid, profileImageUrl))

                    if (friendsProfiles.size == friendsUidList.size) {
                        friendsAdapter.updateFriendsList(friendsProfiles)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching profile for UID $uid: ${e.message}")
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


