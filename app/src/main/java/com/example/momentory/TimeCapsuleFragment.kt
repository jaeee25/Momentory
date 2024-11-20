package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momentory.databinding.FragmentTimeCapsuleBinding

class TimeCapsuleFragment : Fragment() {
    private var _binding: FragmentTimeCapsuleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimeCapsuleBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.createCapsuleButton.setOnClickListener {
            val intent = Intent(activity, CreateCapsuleActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}