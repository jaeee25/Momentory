package com.example.momentory

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityWriteDiaryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.Calendar


class WriteDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteDiaryBinding
    val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWriteDiaryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Firebase Storage 초기화
        storageRef = FirebaseStorage.getInstance().reference

        // SharedPreferences에서 데이터 가져오기 (사용자 이름)
        val sharedPref = getSharedPreferences("ProfileData", Context.MODE_PRIVATE)
        val profileName = sharedPref.getString("profileName", "송이")

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
                    val formattedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
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

        //
        // 갤러리 연동: 갤러리 앱을 통해 사진을 선택하는 기능 추가
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            try {
                val uri = it.data?.data
                if (uri != null) {
                    binding.selectedImage.tag = uri // URI 저장

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

                        // 이미지가 선택되었으므로 VISIBLE로 전환
                        binding.selectedImage.visibility = android.view.View.VISIBLE
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
            binding.shareText.text = "비공개"
            binding.location.text = "위치"
            binding.weather.text = "날씨"
            binding.content.text.clear()
            finish()
        }

        // 저장 버튼 누르면 작성 중인 내용 저장하고 Home Activity로 이동
        // 비공개인 경우 -> 비밀 일기로 저장됨
        // 공개인 경우 -> 공유 일기로 저장됨
        binding.writebtn.setOnClickListener {
            val type: String

            if (binding.shareText.text == "비공개") {
                // 작성한 일기를 비밀 일기에 저장
                type = "secret"
            } else {
                // 작성한 일기를 공유 일기에 저장
                type = "share"
            }
            // 이미지 URL 가져오기

            val documentId = binding.date.text.toString() // 선택한 날짜를 문서 ID로 사용
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val location = binding.location.text.toString()
            val weather = "흐림" // 날씨 API와 연동 가능

            if (documentId.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                // 이미지가 선택된 경우
                val selectedImageUri = binding.selectedImage.tag as? Uri
                if (selectedImageUri != null) {
                    uploadImageToStorage(selectedImageUri, documentId) { imageUrl ->
                        // 이미지 URL을 포함한 데이터 저장
                        val diaryData = mapOf(
                            "title" to title,
                            "content" to content,
                            "date" to documentId,
                            "location" to location,
                            "photoUrl" to imageUrl,
                            "user" to profileName,
                            "weather" to weather
                        )
                        saveDiary(type, documentId, diaryData)
                    }
                } else {
                    // 이미지 없이 데이터 저장
                    val diaryData = mapOf(
                        "title" to title,
                        "content" to content,
                        "date" to documentId,
                        "location" to location,
                        "photoUrl" to null,
                        "user" to profileName,
                        "weather" to weather
                    )
                    saveDiary(type, documentId, diaryData)

                    finish()
                }
            } else {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            }



        }


        //
        // 위치 선택 기능
        val selectLocationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedAddress = result.data?.getStringExtra("location")
                binding.location.text = selectedAddress ?: "위치를 확인할 수 없습니다."
            } else {
                binding.location.text = "위치 선택 취소됨"
            }
        }

        // 위치 선택 버튼 클릭 시 SelectLocationActivity 실행
        binding.selectLoc.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            selectLocationLauncher.launch(intent)
        }

        // 기존의 '위치' 텍스트 설정
        val location = intent.getStringExtra("location")
        binding.location.text = location ?: "(2) 위치 선택"




        //
        // 날씨 (사용자가 설정한 날짜의 날씨 불러오기)
    }

    // 이미지 크기를 줄이기 위한 비율 계산 함수
    private fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

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

    // 파이어베이스에 일기 저장하는 함수
    private fun saveDiary(type:String, documentId: String, diaryData: Map<String, String?>) {
        db.collection("diary")
            .document(type) // "share" 하위에 저장
            .collection("entries") // 모든 일기를 관리하는 서브컬렉션
            .document(documentId) // 날짜 기반으로 문서 생성
            .set(diaryData) // 데이터를 Firestore에 저장
            .addOnSuccessListener {
                Toast.makeText(this, "일기 저장 성공!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "일기 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 이미지 업로드 함수
    private fun uploadImageToStorage(imageUri: Uri, fileName: String, onSuccess: (String) -> Unit) {
        val imageRef = storageRef.child("images/$fileName.jpg")

        contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
            }
            val data = baos.toByteArray()

            // Firebase Storage에 업로드
            imageRef.putBytes(data)
                .addOnSuccessListener {
                    // 업로드가 완료되면 다운로드 URL 가져오기
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        onSuccess(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "이미지 업로드 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    return@addOnFailureListener
                }
        }
    }



}
