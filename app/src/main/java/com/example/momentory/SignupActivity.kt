package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momentory.databinding.ActivitySignupBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val db = FirebaseFirestore.getInstance()
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var isOtpVerified = false  // OTP 인증 여부 표현

    private val RC_SIGN_IN = 9001  // 구글 로그인 결과 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        addPhoneNumberFormatter()

        // 비밀번호 숨김/표시 기능
        binding.seepwSu.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.pwSu.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwSu.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘

        // "인증 요청" 버튼 클릭 (OTP 요청)
        binding.phoneCert.setOnClickListener {
            val phoneNumber = binding.phoneSu.text.toString().trim()
            if (isValidPhoneNumber(phoneNumber)) {
                sendOtp(phoneNumber)
            } else {
                Toast.makeText(this, "유효한 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // "인증" 버튼 클릭 (OTP 인증)
        binding.otpCert.setOnClickListener {
            val otpCode = binding.otp.text.toString().trim()
            if (otpCode.isNotEmpty() && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "OTP를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // "<" 버튼 (이전 화면, 즉 로그인 화면으로 이동)
        binding.toLogin.setOnClickListener {
            finish()
        }

        // 구글로 회원가입 google_su
        binding.googleSu.setOnClickListener {
            signInWithGoogle()
        }

        // "회원가입" 버튼 클릭 (OTP 인증 성공 -> 비밀번호 입력 -> 회원가입)
        binding.signup.setOnClickListener {
            if (isOtpVerified) { // OTP 인증이 완료된 경우에만 회원가입 진행
                val phoneNumber = binding.phoneSu.text.toString().trim()
                val password = binding.pwSu.text.toString().trim()

                if (password.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    // Firestore에 사용자 정보 저장
                    val user = hashMapOf(
                        "phoneNumber" to phoneNumber,
                        "password" to password,
                        "email" to "",
                        "signupMethod" to "normal",
                        "name" to ""
                    )

                    // Firebase Auth에서 UID 가져오기
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        db.collection("users").document(userId) // UID를 문서 ID로 사용
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                                finish() // 회원가입 완료 후 로그인 화면으로 이동
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "데이터 저장 실패: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "OTP 인증이 완료되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (binding.pwSu.transformationMethod is PasswordTransformationMethod) {
            binding.pwSu.transformationMethod = null
            binding.seepwSu.setImageResource(R.drawable.baseline_lock_open_24)
        } else {
            binding.pwSu.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.seepwSu.setImageResource(R.drawable.baseline_lock_outline_24)
        }
        binding.pwSu.setSelection(binding.pwSu.text.length)
    }



    // 전화번호 유효성 검사 함수
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // 한국 전화번호 형식: 010-xxxx-xxxx 또는 010xxxxxxx
        return phoneNumber.matches(Regex("^010-?\\d{4}-?\\d{4}\$"))
    }


    private fun sendOtp(phoneNumber: String) {
        val formattedPhoneNumber = formatPhoneNumber(phoneNumber) // 전화번호 형식 변환
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedPhoneNumber) // 변환된 전화번호 사용
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@SignupActivity, "인증 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verificationId, token)
                    this@SignupActivity.verificationId = verificationId
                    this@SignupActivity.resendToken = token
                    Toast.makeText(this@SignupActivity, "OTP가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    // 전화번호 형식 변환 함수
    private fun formatPhoneNumber(phoneNumber: String): String {
        // 전화번호가 '010-1111-1111' 형식일 경우, 국가 코드 '+82'를 추가한 형식으로 변환
        return if (phoneNumber.startsWith("010")) {
            "+82 ${phoneNumber.replace("-", "").substring(1)}"
        } else {
            phoneNumber // 이미 국제전화번호 형식이면 그대로 반환
        }
    }

    // OTP 인증 후 Firestore에 UID 기반으로 사용자 정보 저장
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // OTP 인증 성공
                    isOtpVerified = true
                    Toast.makeText(this, "OTP 인증 성공", Toast.LENGTH_SHORT).show()
                    // 회원가입 버튼 활성화
                    binding.signup.isEnabled = true
                } else {
                    // OTP 인증 실패
                    isOtpVerified = false
                    binding.signup.isEnabled = false
                    Toast.makeText(this, "OTP 인증 실패: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addPhoneNumberFormatter() {
        binding.phoneSu.addTextChangedListener(object : TextWatcher {
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


    // 구글로 회원가입
    private fun signInWithGoogle() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Firebase에서 제공하는 웹 클라이언트 ID
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                } else {
                    Toast.makeText(this, "Google 계정 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("SignupActivity", "Google Sign-In 실패, 상태 코드: ${e.statusCode}")
                Toast.makeText(this, "Google 로그인 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account?.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userData = mapOf(
                            "phoneNumber" to "", // 공백
                            "password" to "", // 공백
                            "email" to user.email.orEmpty(),
                            "signupMethod" to "google",
                            "name" to user.displayName.orEmpty()
                        )

                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Google 회원가입 완료!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "데이터 저장 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Google 로그인 실패: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

