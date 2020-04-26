package com.chat.okitokichatchat.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.chat.okitokichatchat.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    //private lateinit var remoteConfig: FirebaseRemoteConfig
    //private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = it.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        activityList.add(this)

        //remoteConfig = FirebaseRemoteConfig.getInstance()
        //remoteConfig 로 버튼색 및 스테이터스바 색상 바꾸기 => 3.19 그냥 적용하지 않기로함
        /* val splashBackground = remoteConfig.getString(getString(R.string.remote_config_color))
        window.statusBarColor = Color.parseColor(splashBackground)
        buttonLogin.setBackgroundColor(Color.parseColor(splashBackground))*/

        setTextChangeListener(emailET, emailTIL)
        setTextChangeListener(passwordET, passwordTIL)

        loginProgressLayout.setOnTouchListener { v, event -> true }
    }

    fun setTextChangeListener(et: TextInputEditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }
        })
    }

    //로그인하기
    fun onLogin(v: View) {
        var proceed = true

        if (emailET.text.isNullOrEmpty()) {
            emailTIL.error = "이메일을 입력해 주세요"
            emailTIL.isErrorEnabled = true
            proceed = false
        }

        if (passwordET.text.isNullOrEmpty()) {
            passwordTIL.error = "비밀번호를 입력해 주세요"
            passwordTIL.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            loginProgressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString())
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        loginProgressLayout.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "이메일 또는 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    loginProgressLayout.visibility = View.GONE
                }
        }
    }

    //회원가입하기
    fun onSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

}
