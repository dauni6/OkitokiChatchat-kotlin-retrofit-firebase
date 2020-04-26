package com.chat.okitokichatchat.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.adapters.ChatRoomListAdapter
import com.chat.okitokichatchat.util.Chat
import com.chat.okitokichatchat.util.DATA_CHAT_CHATROOMS
import com.chat.okitokichatchat.util.DATA_CHAT_COMMENTS
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chatroom.*

class ChatRoomFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chatroom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRoomListAdapter = ChatRoomListAdapter(arrayListOf())
        chatRoomList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomListAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        updateChatRoomList()
    }

    private fun updateChatRoomList() {
        chatRoomList?.visibility = View.GONE
        var chatRooms = arrayListOf<Chat>()
        firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).orderByChild("users/$userId").equalTo(true) //내 아이디로 찾기
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(data: DataSnapshot) {
                    for (document in data.children) {
                        val chatRoom = document.getValue(Chat::class.java)
                        if (chatRoom?.users?.containsKey(userId)!!) {
                            chatRooms.add(chatRoom)
                        }
                    }
                    chatRoomListAdapter?.updateChatRooms(chatRooms)
                    chatRoomList?.visibility = View.VISIBLE
                    //Log.d("friends", "${chatRooms.size}")
                }

                override fun onCancelled(data: DatabaseError) {
                    data.toException().printStackTrace()
                }

            })
    }

    override fun onResume() {
        super.onResume()
        updateChatRoomList()
    }

}
