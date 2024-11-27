package com.example.momentory

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
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

        //
        // '날짜' 텍스트 누르면 날짜 선택 가능
        binding.date.setOnClickListener {
            // 현재 날짜를 기본값으로 설정
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // DatePickerDialog 생성
            val datePickerDialog = DatePickerDialog(
                this, // context
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
        // 비공개인 경우 -> 비밀 일기로 저장됨
        // 공개인 경우 -> 공유 일기로 저장됨

        //
        // 갤러리 연동: 갤러리 앱을 통해 사진을 선택하는 기능 추가
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            try {
                val uri = it.data?.data
                if (uri != null) {
                    // 이미지 로딩 및 크기 조정
                    val calRatio = calculateInSampleSize(
                        uri,
                        resources.getDimensionPixelSize(R.dimen.imgWidth),
                        resources.getDimensionPixelSize(R.dimen.imgHeight)
                    )
                    val option = BitmapFactory.Options()
                    option.inSampleSize = calRatio

                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                        binding.selectedImage.setImageBitmap(bitmap) // 선택된 이미지를 표시
                    }
                }
            } catch (e: Exception) {
                Log.e("WriteDiary", "Error loading image", e)
            }
        }

        // 사진 선택 버튼 클릭 이벤트
        binding.selectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        // 취소 버튼 누르면 작성 중인 내용 초기화 & Home Activity로 이동
        binding.cancelbtn.setOnClickListener {
            binding.selectedImage.setImageBitmap(null)
            binding.title.text.clear()
            binding.location.text = "위치"
            binding.weather.text = "날씨"
            binding.content.text.clear()
            finish()
        }

        // 저장 버튼 누르면 작성 중인 내용 저장하고 Home Activity로 이동
        binding.writebtn.setOnClickListener {
            if (binding.shareText.text == "비공개") {
                // 작성한 일기를 비밀 일기에 저장
            } else {
                binding.shareText.text = "비공개"
                // 작성한 일기를 공유 일기에 저장
            }
            finish()
        }

        //
        // 위치

        //
        // 날씨
    }

    // 이미지 크기를 줄이기 위한 비율 계산 함수
    private fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // 원본 이미지 크기 확인
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
