package com.chat.okitokichatchat.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

abstract class BaseActivity : AppCompatActivity() {

    // *firebase
    protected val firebaseAuth = FirebaseAuth.getInstance()
    protected val firebaseRealtimeDB = FirebaseDatabase.getInstance().reference
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid

    protected var activityList = arrayListOf<Activity>()

}
