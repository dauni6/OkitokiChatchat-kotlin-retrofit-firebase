package com.chat.okitokichatchat.adapters

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatListAdapter constructor(val chats: ArrayList<Chat.Comment>, var user: User, var chatRoomUid: String): RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    fun updateChats(newChats: List<Chat.Comment>, newUser: User, newChatRoomUid: String) {
        user = newUser //user은 상대방
        chatRoomUid = newChatRoomUid
        chats.clear()
        chats.addAll(newChats)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = chats.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatListViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
    )

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(chats[position], user)
    }

    inner class ChatListViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val message = v.findViewById<TextView>(R.id.message)
        private val profileImage = v.findViewById<ImageView>(R.id.message_item_userProfileIV)
        private val userName = v.findViewById<TextView>(R.id.message_item_userName)
        private val destinationLayout = v.findViewById<LinearLayout>(R.id.destination_layout)
        private val topLayout = v.findViewById<LinearLayout>(R.id.top_container)
        private val timestamp = v.findViewById<TextView>(R.id.timestamp)
        private val simpleDataFormat = SimpleDateFormat("yyyy.MM.dd HH:mm") //시간설정
        private val readMyCountLeft = v.findViewById<TextView>(R.id.readMyCountLeft)
        private val readOpponentCountRight = v.findViewById<TextView>(R.id.readOpponentCountRight)
        private var peopleCount = 0

        fun bind(comment: Chat.Comment, user: User) {

            //내가 적은 메세지 인지 상대방의 메세지 인지
            if (comment.uid.equals(user.userId)) {  //상대방의 말풍선
                if (user.imageUri.isNullOrEmpty()){
                    profileImage.setImageDrawable(ContextCompat.getDrawable(profileImage.context, R.drawable.default_user))
                } else {
                    profileImage.loadUrl(user.imageUri) //상대방 이미지
                }
                userName.text = user.userName //상대방 이름
                message.text = comment.message //상대방이 적은 메시지
                message.setBackgroundResource(R.drawable.leftbubble)
                getTimeZoneDate(comment)
                topLayout.gravity = Gravity.LEFT
                //setReadCounter(comment, readOpponentCountRight)

                destinationLayout.visibility = View.VISIBLE
                //message.textSize = 25F

            } else { //나의 말풍선
                message.text = comment.message //내가 적은 메시지
                message.setBackgroundResource(R.drawable.rightbubble) //내가 적은 말풍선
                getTimeZoneDate(comment)
                topLayout.gravity = Gravity.RIGHT
               //setReadCounter(comment, readMyCountLeft)

                destinationLayout.visibility = View.INVISIBLE
            }
        }

        private fun setReadCounter(comment: Chat.Comment, readCountTextView: TextView) {
            if (peopleCount == 0) {
                FirebaseDatabase.getInstance().reference.child(DATA_CHAT_CHATROOMS).child(chatRoomUid).child("users")
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(data: DataSnapshot) {
                            val users = data.value as HashMap<String, Boolean> //채팅방 2명의 유저가 들어가 있음
                            peopleCount = users.size
                            val count = peopleCount - comment.readUsers.size //users.size 는 그 방에 참여한 사람수, comment.readUsers.size 는 읽은 사람 수
                            Log.d("count값", "$count")
                            if (count > 0) { //메시지 안 읽은게 남아있으면 숫자 보여주기
                                readCountTextView.text = count.toString()
                                readCountTextView.visibility = View.VISIBLE
                            } else { //상대방이 읽었으면 숫자 안 보여주기
                                readCountTextView.visibility = View.INVISIBLE
                            }
                        }

                        override fun onCancelled(data: DatabaseError) {
                            data.toException().printStackTrace()
                        }
                    })
            } else {
                val count = peopleCount - comment.readUsers.size
                if (count > 0) { //메시지 안 읽은게 남아있으면 숫자 보여주기
                    readCountTextView.text = count.toString()
                    readCountTextView.visibility = View.VISIBLE
                } else { //상대방이 읽었으면 숫자 안 보여주기
                    readCountTextView.visibility = View.GONE
                }
            }
        }

        private fun getTimeZoneDate(comment: Chat.Comment) {
            val date = Date(comment.timestamp as Long )
            val timeZone = TimeZone.getTimeZone("Asia/Seoul")
            simpleDataFormat.timeZone.id = timeZone.toString()
            val time = simpleDataFormat.format(date)
            timestamp.text = time
            Log.d("타임", "$time")
        }
    }

}