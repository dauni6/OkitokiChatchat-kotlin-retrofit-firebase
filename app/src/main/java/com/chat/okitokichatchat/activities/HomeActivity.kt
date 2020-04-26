package com.chat.okitokichatchat.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.fragments.AccountFragment
import com.chat.okitokichatchat.fragments.ChatRoomFragment
import com.chat.okitokichatchat.fragments.PeopleFragment
import com.chat.okitokichatchat.util.DATA_USERS
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager.beginTransaction().add(R.id.home_framelayout, PeopleFragment()).commit() //시작 과 동시에 PeopleFragment 보여주기

        bottomNV.setOnNavigationItemReselectedListener {
            it.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.action_people -> {
                        supportFragmentManager.beginTransaction().replace(R.id.home_framelayout, PeopleFragment()).commit()
                    }
                    R.id.action_chat -> {
                        supportFragmentManager.beginTransaction().replace(R.id.home_framelayout, ChatRoomFragment()).commit()
                    }
                    R.id.action_account -> {
                        supportFragmentManager.beginTransaction().replace(R.id.home_framelayout, AccountFragment()).commit()
                    }
                }
                true
            }
        }

        populatePushTokenToServer() //pushToken update at FirebaseRealtimeDB
    }

    /**
     * 서버에 기기 토큰 저장하기
     **/
    private fun populatePushTokenToServer() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if(it.isSuccessful) {
                    val token = it.result?.token //firebase token 생성
                    Log.d("토큰", "$token")
                    val map = HashMap<String, Any>()
                    map["pushToken"] = token!!

                    firebaseRealtimeDB.child(DATA_USERS).child(userId!!).updateChildren(map)
                }
            }
    }

    /* onShowDialog는 AccountFragment에 있는 Button인데,
    fragment의 view들은 결국 HomeActivity의 뷰 계층에 속해야 하므로
    onClick 메서드를 HomeActivity에서 만들어 줄 수 있다.
    단, onClick attribute는 그대로 fragment.xml에 만들고, 찾을 수 없다는 오류는 뜨지만, 실행에는 문제 없다.
    fun onShowDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        val commentLayout = layoutInflater.inflate(R.layout.dialog_comment, null)
        builder.setView(commentLayout).setPositiveButton("확인", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

            }

        }).setNegativeButton("취소", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

            }

        })
            .setCancelable(false)
            .show()
    }*/


    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseAuth.signOut() //파이어베이스 로그아웃
    }
}
