package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentSharedDiaryBinding
import com.example.momentory.PostAdapter

class SharedDiaryFragment : Fragment() {
    private var _binding: FragmentSharedDiaryBinding? = null
    private val binding get() = _binding!!

    private val postList = listOf(
        // 샘플 데이터
        Post("라멘 맛있다", "06월 12일", "김재희", "일본에서 먹는 라멘 정말 맛있다~", "image_url", 1, 2),
        Post("10월 일상", "08월 18일", "임재서", "중간고사를 준비하다가 정신없이 10월이 흘러갔다...", "image_url", 2, 3),
        Post("방어 먹음", "10월 27일", "최현영", "기름기가 적을까봐 걱정했는데 윤기 가득한 방어!", "image_url", 3, 1)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSharedDiaryBinding.inflate(inflater, container, false)

        // 친구 추가 버튼 클릭 이벤트
        binding.addFriendButton.setOnClickListener {
            val intent = Intent(activity, FriendAddActivity::class.java)
            startActivity(intent)
        }

        // 일기 작성 버튼 클릭 이벤트
        binding.writeDiaryButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        binding.recyclerView.adapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SHARED)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
