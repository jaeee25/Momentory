package com.example.momentory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivityCheckpwBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class CheckpwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckpwBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var verificationId: String? = null
    private var isOtpVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckpwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        // 전화번호 입력 -> - 추가
        addPhoneNumberFormatter()
    }

    private fun setupListeners() {

        // 인증 요청 버튼
        binding.sendMsg.setOnClickListener {
            val phoneNumber = binding.phoneCert.text.toString().trim()
            if (isValidPhoneNumber(phoneNumber)) {
                sendOtp(formatPhoneNumberForFirebase(phoneNumber))
            } else {
                Toast.makeText(this, "유효한 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 인증 버튼
        binding.certification.setOnClickListener {
            val otpCode = binding.certNum.text.toString().trim()
            if (otpCode.isNotEmpty() && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                verifyOtp(credential)
            } else {
                Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 비밀번호 변경 버튼
        binding.changePw.setOnClickListener {
            val newPassword = binding.pwNew.text.toString().trim()
            val phoneNumber = binding.phoneCert.text.toString().trim()

            if (newPassword.isNotEmpty() && isOtpVerified) {
                updatePassword(phoneNumber, newPassword)
            } else {
                Toast.makeText(this, "새로운 비밀번호를 입력하거나 인증을 완료해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 이전 버튼 클릭 시 로그인 화면으로 이동
        binding.toLogin.setOnClickListener {
            finish()
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^010-?\\d{4}-?\\d{4}\$"))
    }

    private fun formatPhoneNumberForFirebase(phoneNumber: String): String {
        // Firestore 및 Firebase에 저장/조회할 국제화된 전화번호 형식으로 변환
        return if (phoneNumber.startsWith("010")) {
            "+82 ${phoneNumber.replace("-", "").substring(1)}"
        } else {
            phoneNumber // 이미 국제전화번호 형식이면 그대로 반환
        }
    }

    private fun sendOtp(formattedPhoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    verifyOtp(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@CheckpwActivity, "인증 요청 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@CheckpwActivity.verificationId = verificationId
                    Toast.makeText(this@CheckpwActivity, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isOtpVerified = true
                    binding.certComplete.visibility = android.view.View.VISIBLE
                    binding.pwChangeLayout.visibility = android.view.View.VISIBLE
                    binding.pwNew.isEnabled = true
                    binding.changePw.isEnabled = true
                    Toast.makeText(this, "OTP 인증 성공!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "OTP 인증 실패: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePassword(phoneNumber: String, newPassword: String) {
        val userRef = firestore.collection("user").document(phoneNumber)
        userRef.update("password", newPassword) // 비밀번호 암호화 필요
            .addOnSuccessListener {
                Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "비밀번호 변경 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addPhoneNumberFormatter() {
        binding.phoneCert.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val formatted = s.toString().replace("-", "")
                    .replace(Regex("(\\d{3})(\\d{4})(\\d{4})"), "$1-$2-$3")
                s?.replace(0, s.length, formatted)

                isEditing = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
