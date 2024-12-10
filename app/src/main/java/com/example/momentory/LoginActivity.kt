package com.example.momentory

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.momentory.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val RC_SIGN_IN = 9001  // 구글 로그인 결과 코드

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityLoginBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        addPhoneNumberFormatter()

        // 비밀번호 입력 칸에 있는 자물쇠 아이콘 누르면 아이콘 변경 및 비밀번호 숨김 / 표시
        binding.seepwLi.setOnClickListener {
            togglePasswordVisibility()
        }


        // 처음 시작 시 비밀번호 숨김 상태로 설정
        binding.pwLi.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24) // 잠금 아이콘

        // Log In 버튼 클릭 이벤트
        binding.login.setOnClickListener {
            val phoneNumber = binding.phoneLi.text.toString().trim()
            val password = binding.pwLi.text.toString().trim()

            if (phoneNumber.isNotEmpty() && password.isNotEmpty()) {
                loginUser(phoneNumber, password)
            } else {
                Toast.makeText(this, "전화번호와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // Forget Password 클릭 이벤트
        binding.forgotPw.setOnClickListener {
            val intent = Intent(this, CheckpwActivity::class.java)
            startActivity(intent)
        }

        // Sign Up 클릭 이벤트
        binding.toSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // "Google 로그인" 클릭 시
        binding.googleLi.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun togglePasswordVisibility() {
        if (binding.pwLi.transformationMethod is PasswordTransformationMethod) {
            binding.pwLi.transformationMethod = null
            binding.seepwLi.setImageResource(R.drawable.baseline_lock_open_24)
        } else {
            binding.pwLi.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.seepwLi.setImageResource(R.drawable.baseline_lock_outline_24)
        }
        binding.pwLi.setSelection(binding.pwLi.text.length)
    }

    // 사용자 로그인 처리
    private fun loginUser(phoneNumber: String, password: String) {
        db.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val storedPassword = document.getString("password")

                    if (storedPassword == password) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "로그인 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addPhoneNumberFormatter() {
        binding.phoneLi.addTextChangedListener(object : TextWatcher {
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

    // Google 로그인 처리
    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 웹 클라이언트 ID
            .requestEmail() // 이메일 정보 요청
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account?.idToken == null) {
            Toast.makeText(this, "Google ID 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    checkOrAddGoogleUser(user)
                } else {
                    Toast.makeText(this, "Firebase 인증 실패: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun checkOrAddGoogleUser(user: FirebaseUser?) {
        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "회원가입 후 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Firestore 확인 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
