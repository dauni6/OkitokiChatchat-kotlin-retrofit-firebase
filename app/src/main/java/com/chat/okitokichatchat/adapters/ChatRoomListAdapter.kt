package com.chat.okitokichatchat.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.activities.MessageActivity
import com.chat.okitokichatchat.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomListAdapter constructor(val chatRooms: ArrayList<Chat>) : RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomListViewHolder>() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseRealtimeDB = FirebaseDatabase.getInstance().reference

    fun updateChatRooms(newChatRooms: List<Chat>) {
        chatRooms.clear()
        chatRooms.addAll(newChatRooms)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = chatRooms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatRoomListViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_chatlist, parent, false)
    )

    override fun onBindViewHolder(holder: ChatRoomListViewHolder, position: Int) {
        holder.bind(chatRooms[position])
    }

    inner class ChatRoomListViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val layout = v.findViewById<ViewGroup>(R.id.chatUserListLayout)
        private val chatRoomImage = v.findViewById<ImageView>(R.id.chatRoomThumbNail)
        private val chatRoomTitle = v.findViewById<TextView>(R.id.chatRoomTitle)
        private val chatRoomLastMessage = v.findViewById<TextView>(R.id.chatRoomLastMessage)
        private val chatRoomLastMessageTimestamp = v.findViewById<TextView>(R.id.chatRoomLastMessageTimestamp)

        private var destinationUid: String? = null
        private var destinationToken: String? = null
        private var chatRoomUid: String? = null

        private val simpleDataFormat = SimpleDateFormat("yyyy.MM.dd HH:mm") //시간설정

        fun bind(chatRoom : Chat) {
            //채팅방 유저 체크
            for (destinationUser in chatRoom.users?.keys!!) {
                if (destinationUser != userId) {
                    destinationUid = destinationUser
                    firebaseRealtimeDB.child(DATA_USERS).child(destinationUid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(data: DataSnapshot) {
                                val targetUser = data.getValue(User::class.java)
                                targetUser?.let {
                                    chatRoomImage.loadUrl(targetUser.imageUri, R.drawable.default_user)
                                    chatRoomTitle.text = targetUser.userName
                                    destinationToken = targetUser.pushToken
                                    getTargetChatRoomUid() //타겟 채팅방 uid 가져오기
                                    goToTargetDestinationChatRoom() //해당 채팅방으로 가기
                                }
                            }
                            override fun onCancelled(data: DatabaseError) {
                                data.toException().printStackTrace()
                            }
                        })

                } // if

            } // for end




        } //bind end

        private fun getTargetChatRoomUid() {
            firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).orderByChild("users/${userId}").equalTo(true)
                .addListenerForSingleValueEvent( object : ValueEventListener{
                    override fun onDataChange(data: DataSnapshot) {
                        for (item in data.children) {
                            val result = item.getValue(Chat::class.java)
                            if (result?.users?.containsKey(destinationUid)!!){
                                chatRoomUid = item.key //채팅룸 uid 가져오기
                                getTargetChatMessages()
                            }
                        }

                    }
                    override fun onCancelled(data: DatabaseError) {
                        data.toException().printStackTrace()
                    }
                })
        }

        private fun getTargetChatMessages() {
            var comments = arrayListOf<Chat.Comment>()
            var lastMessageIndex: Int
            firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).child(chatRoomUid!!).child(DATA_CHAT_COMMENTS).addListenerForSingleValueEvent( object : ValueEventListener{
                override fun onDataChange(data: DataSnapshot) {
                    for (item in data.children) {
                        val comment = item.getValue(Chat.Comment::class.java)
                        comment?.let { comments.add(comment) }
                    }
                    lastMessageIndex = comments.lastIndex
                    chatRoomLastMessage.text = comments[lastMessageIndex].message
                    getTimeZoneDate(comments[lastMessageIndex])
                }
                override fun onCancelled(data: DatabaseError) {
                   data.toException().printStackTrace()
                }
            })

        }

        private fun goToTargetDestinationChatRoom() {
            layout.setOnClickListener { v: View? ->
                val intent = Intent(v?.context, MessageActivity::class.java)
                intent.putExtra("destination", destinationUid)
                intent.putExtra("destinationToken", destinationToken)
                v?.context?.startActivity(intent)
            }
        }

        private fun getTimeZoneDate(comment: Chat.Comment) {
            val date = Date(comment.timestamp as Long )
            val timeZone = TimeZone.getTimeZone("Asia/Seoul")
            simpleDataFormat.timeZone.id = timeZone.toString()
            val time = simpleDataFormat.format(date)
            chatRoomLastMessageTimestamp.text = time
            Log.d("타임", "$time")
        }

    } //recycler inner class

}