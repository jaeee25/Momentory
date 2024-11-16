package com.example.momentory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.content.Intent



class SecretDiaryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_secret_diary, container, false)

        // 일기 작성
        val writeDiaryButton: ImageView = view.findViewById(R.id.write_secret_button)
        writeDiaryButton.setOnClickListener {
            val intent = Intent(activity, WriteDiaryActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
