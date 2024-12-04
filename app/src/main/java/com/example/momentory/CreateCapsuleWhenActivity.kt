package com.example.momentory

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import com.example.momentory.databinding.ActivityCreateCapsuleWhenBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class CreateCapsuleWhenActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    val binding: ActivityCreateCapsuleWhenBinding by lazy {
        ActivityCreateCapsuleWhenBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setSupportActionBar(binding.capsuleWhenToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val selectedFriends = intent.getStringArrayListExtra("selectedFriends") ?: emptyList()
        val locale = Locale("ko", "KR")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val calendar = binding.datePicker
        binding.completeCapsuleButton.setOnClickListener {
            val year = calendar.year
            val month = calendar.month
            val dayOfMonth = calendar.dayOfMonth

            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time

            if(selectedDate.before(Date())) {
                Toast.makeText(this, "미래의 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createTimeCapsule(selectedFriends, selectedDate)
//            Log.d("CreateCapsuleWhenActivity", "year: $year, month: $month, dayOfMonth: $dayOfMonth")
            Log.d("CreateCapsuleWhenActivity","$selectedDate")
            Log.d("CreateCapsuleWhenActivity","$selectedFriends")
        }
    }


    private fun createTimeCapsule(selectedFriends: List<String>, selectedDate: Date) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentUserId = "vb6wQZCFD1No8EYwjmQ4" // 임시 ID
        val friends = selectedFriends.toMutableList()
        friends.add(currentUserId)

        val timeCapsuleData = hashMapOf(
            "friends" to friends,
            "unlockDate" to selectedDate,
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to "pending",
        )

        db.collection("timeCapsules")
            .add(timeCapsuleData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "타임캡슐이 생성되었습니다!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // 액티비티 종료
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "타임캡슐 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("CreateCapsuleWhenActivity", "Error creating time capsule", e)
            }
    }
}