package com.example.momentory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import android.widget.ImageButton


class TimeCapsuleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_time_capsule, container, false)

        // 캡슐 생성
        val createCapsuleButton: ImageButton  = view.findViewById(R.id.create_capsule_button)
        createCapsuleButton.setOnClickListener {
            val intent = Intent(activity, CreateCapsuleActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
