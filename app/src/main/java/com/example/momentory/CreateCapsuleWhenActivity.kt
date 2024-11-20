package com.example.momentory

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.example.momentory.databinding.ActivityCreateCapsuleWhenBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Locale

class CreateCapsuleWhenActivity : AppCompatActivity() {
    val binding: ActivityCreateCapsuleWhenBinding by lazy {
        ActivityCreateCapsuleWhenBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val locale = Locale("ko", "KR")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val calendar = binding.datePicker
        binding.completeCapsuleButton.setOnClickListener {
            val year = calendar.year
            val month = calendar.month+1
            val dayOfMonth = calendar.dayOfMonth
            Log.d("CreateCapsuleWhenActivity", "year: $year, month: $month, dayOfMonth: $dayOfMonth")
        }
    }
}