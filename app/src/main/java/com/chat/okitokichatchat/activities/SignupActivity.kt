package com.chat.okitokichatchat.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.util.DATA_USERS
import com.chat.okitokichatchat.util.DATA_USER_PROFILE_IMAGES
import com.chat.okitokichatchat.util.REQUEST_CODE_PHOTO
import com.chat.okitokichatchat.util.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : BaseActivity() {

    //private val firebaseAuth = FirebaseAuth.getInstance()
    //private val firebaseRealtimeDB = FirebaseDatabase.getInstance().reference
    private val firebaseStorage = FirebaseStorage.getInstance().reference

    private var imageUri: Uri? = null
    private var convertedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setTextChangeListener(usernameET, usernameTIL)
        setTextChangeListener(emailET, emailTIL)
        setTextChangeListener(passwordET, passwordTIL)

        signupProgressLayout.setOnTouchListener { v, event -> true }
    }

    private fun setTextChangeListener(et: TextInputEditText, til: TextInputLayout) {
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

    fun onPickProfileImage(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        //intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO) //startActivityForResult 에 모든 이벤트가 모여서 request code 값으로 switch문에 의해 원하는 부분으로 이벤트가 이동함
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            profileIV.setImageURI(data?.data) //이미지바꿔주기
            imageUri = data?.data
        }
    }

    //회원가입하기
    fun onSignup(v: View) {
        var proceed = true
        if(usernameET.text.isNullOrEmpty()) {
            usernameTIL.error = "닉네임을 입력해 주세요!"
            usernameTIL.isErrorEnabled = true
            proceed = false
        }

        if (emailET.text.isNullOrEmpty()) {
            emailTIL.error = "이메일을 입력해 주세요!"
            emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (passwordET.text.isNullOrEmpty()) {
            passwordTIL.error = "비밀번호를 입력해 주세요!"
            passwordTIL.isErrorEnabled = true
            proceed = false
        }

        if (imageUri == null){
            Toast.makeText(this, "프로필 사진을 선택해 주세요!", Toast.LENGTH_SHORT).show()
            proceed = false
        }

        if (proceed) {
            signupProgressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString())
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "이미 사용중인 이메일 입니다.", Toast.LENGTH_SHORT).show()
                        signupProgressLayout.visibility = View.GONE
                    } else {
                        val userProfileChangeRequest = UserProfileChangeRequest.Builder().setDisplayName(usernameET.text.toString()).build()
                        it.result?.user?.updateProfile(userProfileChangeRequest) //유저이름을 담기

                        //firebaseStorage 이미지 파일 저장하기
                        val filepath = firebaseStorage.child(DATA_USER_PROFILE_IMAGES).child(firebaseAuth.uid!!)
                        filepath.putFile(imageUri!!)
                            .addOnSuccessListener {
                                filepath.downloadUrl
                                    .addOnSuccessListener {
                                        convertedImageUri = it.toString()
                                        //리얼타임 데이터베이스 유저정보 저장하기
                                        val userName = usernameET.text.toString()
                                        val imageUri = convertedImageUri
                                        val userId = firebaseAuth.uid
                                        val user = User(userName, imageUri, userId)
                                        firebaseRealtimeDB.child(DATA_USERS).child(firebaseAuth.uid!!).setValue(user)
                                        Toast.makeText(this@SignupActivity, "회원가입 완료.", Toast.LENGTH_SHORT).show()
                                        signupProgressLayout.visibility = View.GONE
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        it.printStackTrace()
                                    }
                            }
                            .addOnFailureListener {
                                it.printStackTrace()
                                Log.d("imageUriDownloadFail", "imageUriDownloadFail")
                            }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    Log.d("signup error", "createUserWithEmailAndPassword error")
                    signupProgressLayout.visibility = View. GONE
                }
        }
    }
    
    //로그인하기
    fun onLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}
