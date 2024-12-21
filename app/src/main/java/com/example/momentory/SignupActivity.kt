package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var isGoogle = false  // 구글 로그인 결과 코드

    private val googleUID = firebaseAuth.currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // 비밀번호 입력 칸에 있는 자물쇠 아이콘 누르면 아이콘 변경 및 비밀번호 숨김 / 표시
        binding.seepwSU.setOnClickListener {
            togglePasswordVisibility()
        }


        // 처음 시작 시 비밀번호 숨김 상태로 설정
        binding.pwdSU.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwSU.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘

        // "<" 버튼 (이전 화면, 즉 로그인 화면으로 이동)
        binding.toLogin.setOnClickListener {
            finish()
        }

        ////////////////////
        val currentUser = firebaseAuth.currentUser

        // 구글 로그인한 사용자 구분
        if (currentUser != null) {
            // 구글 로그인한 사용자일 경우
            if (currentUser.providerData.any { it.providerId == "google.com" }) {
                isGoogle = true
            }
        }

        binding.googleSignUp.setOnClickListener {
            // 구글로 회원가입 한 경우 loginActivity로 이동
            if (isGoogle) {
                googleSignup()
                finish()
            } else {
                emailSignup()
            }
        }

    }

    private fun togglePasswordVisibility() {
        if (binding.pwdSU.transformationMethod is PasswordTransformationMethod) {
            binding.pwdSU.transformationMethod = null
            binding.seepwSU.setImageResource(R.drawable.baseline_lock_open_24)
        } else {
            binding.pwdSU.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.seepwSU.setImageResource(R.drawable.baseline_lock_outline_24)
        }
        binding.pwdSU.setSelection(binding.pwdSU.text.length)
    }

    private fun emailSignup() {
        val email = binding.emailSU.text.toString().trim()  // 수정: EditText에서 값 가져오기
        val password = binding.pwdSU.text.toString().trim()

        // 이메일과 비밀번호 입력 체크
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 이메일 형식 체크
        if (!isValidEmail(email)) {
            Toast.makeText(this, "잘못된 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase에 이메일과 비밀번호로 회원가입
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        val user = hashMapOf(
                            "email" to email,
                            "signupMethod" to "email"
                        )

                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()

                                // 회원가입 후 ProfileActivity로 이동
                                val intent = Intent(this, SingnUpProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "데이터 저장 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val exceptionMessage = task.exception?.localizedMessage ?: "회원가입에 실패했습니다."
                    Toast.makeText(this, exceptionMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }



    private fun googleSignup() {
        val phoneNumber = binding.emailSU.text.toString().trim()

        // Firestore에 사용자 정보 저장

        // Firebase Auth에서 UID 가져오기
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            if (googleUID != null) {
                db.collection("users").document(googleUID) // UID를 문서 ID로 사용
                    .update("phoneNumber", phoneNumber)
                    .addOnSuccessListener {
                        Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "데이터 저장 실패: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "구글 로그인 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}



