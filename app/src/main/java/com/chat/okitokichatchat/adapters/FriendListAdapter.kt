package com.chat.okitokichatchat.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.activities.MessageActivity
import com.chat.okitokichatchat.util.User
import com.chat.okitokichatchat.util.loadUrl

class FriendListAdapter constructor(val friends: ArrayList<User>): RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder>() {

    fun updateFriends(newFriends: List<User>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged() //새로고침
    }

    override fun getItemCount(): Int = friends.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FriendListViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
    )

    override fun onBindViewHolder(holder: FriendListViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    inner class FriendListViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val layout = v.findViewById<ViewGroup>(R.id.item_container)
        private val userProfileImage = v.findViewById<ImageView>(R.id.userProfileIV)
        private val userName = v.findViewById<TextView>(R.id.userNameTV)
        private val userStateComemnt = v.findViewById<TextView>(R.id.userStateCommentTV)

        fun bind(friend: User?) {
            userName.text = friend?.userName
            userStateComemnt.text = friend?.comment
            if (friend?.imageUri.isNullOrEmpty()) {
                userProfileImage.setImageDrawable(ContextCompat.getDrawable(userProfileImage.context, R.drawable.default_user))
            } else {
                userProfileImage.loadUrl(friend?.imageUri)
            }
            layout.setOnClickListener { v: View? ->
                val intent = Intent(v?.context, MessageActivity::class.java)
                intent.putExtra("destination", friend?.userId)
                intent.putExtra("destinationToken", friend?.pushToken)
                //val activityOptions = ActivityOptions.makeCustomAnimation(v?.context, R.anim.fromright, R.anim.toleft) 여기서 애니메이션 효과 줘도 되지만, activity onCreate() 에서 줘도 된다. => onCreate()에 설정함 액티비티 종료시는 onPause()에서 설정.
                //v?.context?.startActivity(intent, activityOptions.toBundle())
                v?.context?.startActivity(intent)
            }
        }
    }

}