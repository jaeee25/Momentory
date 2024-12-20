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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val documentId = document.id
        val unlockDate = document.getDate("unlockDate") ?: Date()
        val createDate = document.getDate("createdAt") ?: Date()
        val imageRes = R.drawable.baseline_lock_open_white_24
        val friends = document.get("friends") as? List<String> ?: emptyList()

        return TimeCapsuleItem(
            capsuleId = documentId,
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
                val intent = Intent(requireContext(), OpenTimeCapsuleActivity::class.java).apply {
                    putExtra("timeCapsuleId", selectedItem.capsuleId)
                    putExtra("timeCapsuleTitle",DateUtils.formatDateWithYear(selectedItem.releaseDate))
                    putStringArrayListExtra("timeCapsuleFriends", ArrayList(selectedItem.friends))
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