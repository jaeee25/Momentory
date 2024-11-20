package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momentory.databinding.FragmentSecretDiaryBinding
import com.example.momentory.PostAdapter
import androidx.recyclerview.widget.LinearLayoutManager

class SecretDiaryFragment : Fragment() {
    private var _binding: FragmentSecretDiaryBinding? = null
    private val binding get() = _binding!!

    private val postList = listOf(
        // 비밀 일기의 샘플 데이터
        Post("비밀 일기 1", "11월 20일", "", "시험이 안끝난다", "image_url", 0, 0),
        Post("비밀 일기 2", "11월 18일", "", "과제가 안끝난다", "image_url", 0, 0)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecretDiaryBinding.inflate(inflater, container, false)

        // RecyclerView 설정
        binding.recyclerView.adapter = PostAdapter(postList, PostAdapter.VIEW_TYPE_SECRET)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


