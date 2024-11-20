package com.example.momentory

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityWriteDiaryBinding
import java.util.Calendar

class WriteDiaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWriteDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // < 버튼 누르면 뒤로가기 (Home Activity로 이동)
        binding.toHome.setOnClickListener {
            finish()
        }

        // '날짜' 텍스트 누르면 날짜 선택 가능
        binding.date.setOnClickListener {
            // 현재 날짜를 기본값으로 설정
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // DatePickerDialog 생성
            val datePickerDialog = DatePickerDialog(
                it.context, // context
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 사용자가 날짜를 선택했을 때 실행되는 코드
                    val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    binding.date.text = formattedDate // 선택된 날짜를 텍스트 뷰에 표시
                },
                year, month, day // 초기 날짜 설정
            )

            // DatePickerDialog 띄우기
            datePickerDialog.show()
        }

        // 비공개 버튼 누르면 공개로 전환 (공개 버튼 누르면 비공개로 전환)
        binding.shareText.setOnClickListener {
            if (binding.shareText.text == "비공개") {
                binding.shareText.text = "공개"
            } else {
                binding.shareText.text = "비공개"
            }
        }

        // 카메라 아이콘 누르면 사진 선택 가능
        binding.selectedImage.setOnClickListener {
            // 사진 선택
            // 선택된 이미지가 보여짐
            // 카메라 아이콘 사라짐
        }

        // 위치 아이콘 누르면 위치 선택 가능 -> '위치' 텍스트에 받아온 위치 표시 (텍스트)

        // 날씨 누르면 날씨 선택 가능 -> '날씨' 텍스트에 받아온 날씨 표시 (텍스트) -- 맑음 / 흐림 / 비 / 눈


        // 취소 버튼 누르면 작성 중인 내용 초기화 & Home Activity로 이동
        binding.cancelbtn.setOnClickListener {
            binding.selectedImage.setImageResource(0)
            binding.title.text.clear()
            binding.location.text = "위치"
            binding.weather.text = "날씨"
            binding.content.text.clear()
            finish()
        }

        // 저장 버튼 누르면 작성 중인 내용 저장하고 Home Activity로 이동
        binding.writebtn.setOnClickListener {
            if (binding.shareText.text == "비공개") {
                // 직성한 일기를 비밀 일기에 저장
            } else {
                binding.shareText.text = "비공개"
                // 작성한 일기를 공유 일기에 저장
            }

            finish()
        }

    }
}
