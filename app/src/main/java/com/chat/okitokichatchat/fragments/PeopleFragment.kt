package com.chat.okitokichatchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.adapters.FriendListAdapter
import com.chat.okitokichatchat.util.DATA_USERS
import com.chat.okitokichatchat.util.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_people.*

class PeopleFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //listener = FriendListenerImpl()
        friendsListAdapter = FriendListAdapter(arrayListOf())
        //friendsListAdapter?.setListener(listener)
        friendList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsListAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        updateFriendList()
    }

    private fun updateFriendList() {
        friendList?.visibility = View.GONE
        var friends = arrayListOf<User>()
        firebaseRealtimeDB.child(DATA_USERS).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                        for (document in data.children) {
                            val friend = document.getValue(User::class.java)
                            if (friend?.userId == userId) continue //본인은 리스트에 넣지않기
                            friend?.let { friends.add(friend) }
                    }
                    friendsListAdapter?.updateFriends(friends)
                    friendList?.visibility = View.VISIBLE
                }
                override fun onCancelled(data: DatabaseError) {
                    data.toException().printStackTrace()
                }
            }
        )
    }

}
