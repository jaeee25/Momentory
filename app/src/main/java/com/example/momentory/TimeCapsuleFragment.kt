package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentTimeCapsuleBinding

class TimeCapsuleFragment : Fragment() {

    private var _binding: FragmentTimeCapsuleBinding? = null
    private val binding get() = _binding!!

    // 샘플 데이터
    private val timeCapsuleList = listOf(
        TimeCapsuleItem("~2024.11.01", "10월 13일", R.drawable.ic_sample_image),
        TimeCapsuleItem("~2025.01.01", "9월 18일", R.drawable.ic_lock),
        TimeCapsuleItem( "~2025.12.31", "8월 17일", R.drawable.ic_lock)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeCapsuleBinding.inflate(inflater, container, false)

        setupRecyclerView() // RecyclerView 설정
        setupCreateCapsuleButton() // 연필 버튼 클릭 이벤트 설정

        return binding.root
    }

    private fun setupRecyclerView() {
        // RecyclerView 초기화
        binding.timeCapsuleRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TimeCapsuleAdapter(timeCapsuleList)
        }
    }

    private fun setupCreateCapsuleButton() {
        // 연필 모양 버튼 클릭 이벤트 처리
        binding.createCapsuleButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateCapsuleActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
