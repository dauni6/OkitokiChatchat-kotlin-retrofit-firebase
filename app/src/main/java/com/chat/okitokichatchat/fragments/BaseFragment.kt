package com.chat.okitokichatchat.fragments

import androidx.fragment.app.Fragment
import com.chat.okitokichatchat.adapters.ChatRoomListAdapter
import com.chat.okitokichatchat.adapters.FriendListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

abstract class BaseFragment : Fragment() {
    protected var friendsListAdapter: FriendListAdapter? = null
    protected var chatRoomListAdapter: ChatRoomListAdapter? = null
    protected val firebaseRealtimeDB = FirebaseDatabase.getInstance().reference
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid

}
