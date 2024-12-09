package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentory.databinding.FragmentTimeCapsuleBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.Date

class TimeCapsuleFragment : Fragment() {

    private var _binding: FragmentTimeCapsuleBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val timeCapsuleList = mutableListOf<TimeCapsuleItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeCapsuleBinding.inflate(inflater, container, false)

        setupRecyclerView() // RecyclerView 설정
        setupCreateCapsuleButton() // 연필 버튼 클릭 이벤트 설정
        fetchTimeCapsules()

        return binding.root
    }

    private fun fetchTimeCapsules() {
        val currentUserId = "vb6wQZCFD1No8EYwjmQ4" // 현재 사용자 ID (FirebaseAuth로 교체 가능)

        db.collection("timeCapsules")
            .whereArrayContains("friends", currentUserId) // friends 배열에 currentUserId가 포함된 문서만 조회
            .get()
            .addOnSuccessListener { documents ->
                timeCapsuleList.clear()
                for (document in documents) {
                    val timeCapsuleItem = mapDocumentToTimeCapsuleItem(document)
                    timeCapsuleList.add(timeCapsuleItem)
                }
                setupRecyclerView() // 데이터를 가져온 후 RecyclerView 설정
            }
            .addOnFailureListener { exception ->
                Log.e("TimeCapsuleFragment", "Error getting documents: ", exception)
            }
    }

    private fun mapDocumentToTimeCapsuleItem(document: QueryDocumentSnapshot): TimeCapsuleItem {
        val unlockDate = document.getDate("unlockDate") ?: Date()
        val createDate = document.getDate("createdAt") ?: Date()
        val imageRes = null // 기본 이미지
        val friends = document.get("friends") as? List<String> ?: emptyList()

        return TimeCapsuleItem(
            releaseDate = unlockDate,
            createDate = createDate,
            imageRes = imageRes,
            friends = friends
        )
    }

    private fun setupRecyclerView() {
        binding.timeCapsuleRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TimeCapsuleAdapter(timeCapsuleList) { selectedItem ->
                // 클릭된 TimeCapsuleItem을 기반으로 OpenTimeCapsuleActivity 실행
                val intent = Intent(requireContext(), OpenTimeCapsuleActivity::class.java).apply {
                    putExtra("releaseDate", selectedItem.releaseDate.time) // Date를 밀리초로 전달
                    putExtra("createDate", selectedItem.createDate.time)
                    putExtra("friends", ArrayList(selectedItem.friends)) // List를 ArrayList로 변환
                }
                startActivity(intent)
            }
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