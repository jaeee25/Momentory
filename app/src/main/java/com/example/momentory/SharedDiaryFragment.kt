package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentSharedDiaryBinding

class SharedDiaryFragment : Fragment() {
    private var _binding: FragmentSharedDiaryBinding? = null
    private val binding get() = _binding!!

    // 샘플 게시글 데이터
    private val postList = listOf(
        Post("라멘 맛있다", "11월 12일", "김재희", "일본에서 먹는 라멘 정말 맛있다~", "image_url", 1, 2),
        Post("10월 일상", "10월 18일", "임재서", "중간고사를 준비하다가 정신없이 10월이 흘러갔다...이거어디까지늘어날지궁금하다어디까지눌어날지궁금하다궁금하다궁금하다궁금하다", "image_url", 2, 3),
        Post("방어 먹음", "9월 27일", "최원영", "기름기가 적을까봐 걱정했는데 윤기 가득한 방어!", "image_url", 3, 1)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSharedDiaryBinding.inflate(inflater, container, false)

        // 친구 추가 버튼 클릭 이벤트
        binding.addFriendButton.setOnClickListener {
            val intent = Intent(activity, FriendsAddActivity::class.java)
            startActivity(intent)
        }

        // 연필 모양 버튼 클릭 이벤트 (글 작성 화면으로 이동)
        binding.writeDiaryButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SHARED) { post, position ->
            // 게시글 클릭 이벤트 (CommentActivity로 이동)
            val intent = Intent(activity, CommentActivity::class.java).apply {
                putExtra("postTitle", post.title)
                putExtra("postContent", post.content)
                putExtra("postAuthor", post.author)
                putExtra("postDate", post.date)
                putExtra("postImageUrl", post.imageUrl)
            }
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
