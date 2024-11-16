package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class SharedDiaryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shared_diary, container, false)

        // 친구 추가 버튼
        val addFriendButton: ImageView = view.findViewById(R.id.add_friend_button)
        addFriendButton.setOnClickListener {
            val intent = Intent(activity, FriendAddActivity::class.java)
            startActivity(intent)
        }

        // 일기 작성 버튼
        val writeDiaryButton: ImageView = view.findViewById(R.id.write_diary_button)
        writeDiaryButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
