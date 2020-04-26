package com.chat.okitokichatchat.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.adapters.ChatListAdapter
import com.chat.okitokichatchat.interfaces.PushFcmApi
import com.chat.okitokichatchat.interfaces.PushFcmApiService
import com.chat.okitokichatchat.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_message.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageActivity : BaseActivity() {

    /**
     * log tags
     * */
    private val PUSH_CALLBACK_RESULT_SUCCESS = "PUSH_CALLBACK_RESULT_SUCCESS"
    private val PUSH_CALLBACK_RESULT_FAILURE = "PUSH_CALLBACK_RESULT_FAILURE"
    private val CHATROOM_INITIALIZE_FAILURE = "CHATROOM_INITIALIZE_FAILURE"
    private val SUCCESS = "success"
    private val FAILURE = "failure"

    private var destinationUid: String? = null //상대방 uid
    private var destinationToken: String? = null //상대방 디바이스 토큰

    private var chatRoomUid: String? = null
    private var chatListAdapter: ChatListAdapter? = null
    
    /**
     * Back-key 누르는 순간 database-watching 하는 것을 끄기 위한 properties
     **/
    private var databaseReference: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null

    private var comments: ArrayList<Chat.Comment>? = null //ChatListAdpater에 보낼 채팅내용 배열

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        overridePendingTransition(R.anim.fromright, R.anim.toleft) //액티비티 실행시 오른쪽으로 열리는 애니메이션

            if(intent.hasExtra("destination") && intent.hasExtra("destinationToken")) { //FriendListAdapter 와 ChatRoomListAdapter 로 부터 받은 intent는 onCreate에서 전달받은 intent가 있다면 바로 사용 가능하게 끔 되어있다.
                destinationUid = intent.getStringExtra("destination") //상대방의 uid
                destinationToken = intent.getStringExtra("destinationToken") //상대방의 디바이스 토큰
            } else {
                Toast.makeText(this, "탈퇴한 회원이거나 상대방을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        
        chatListAdapter = ChatListAdapter(arrayListOf(), User(), "") //adapter초기화

        //리사이클러뷰 설정
        chatRoomList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }

        chatRoomInitialize() //이미 대화한 목록이 있다면 바로 보여주기 위함
    }

    /**
     * 채팅방(chatRoomUid) initialize
     */
    private fun chatRoomInitialize() {
        firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).orderByChild("users/${userId.toString()}").equalTo(true) //1.나의 uid 찾기
            .addListenerForSingleValueEvent( object : ValueEventListener{
                override fun onDataChange(data: DataSnapshot) {
                    for (item in data.children) {
                        val result = item.getValue(Chat::class.java)
                        if (result?.users?.containsKey(destinationUid)!!){ //2. 상대방 uid로 찾기
                            chatRoomUid = item.key //방 고유번호 초기화
                            chatRoomUid?.let {//방이 존재한다면 이미 대화한 내용을 바로 보여주기
                                updateChatList()
                            }
                        }
                    }
                }
                @SuppressLint("LongLogTag")
                override fun onCancelled(data: DatabaseError) {
                    Log.d(CHATROOM_INITIALIZE_FAILURE, FAILURE)
                    data.toException()
                }
            })
    }

    /**
     * 메세지 보내기 버튼
     * 채팅방 만들기(chatRoomUid 초기화)
     **/
    fun onSendMessageButton(v: View) {
        var proceed = true
        if (messageET.text.isNullOrEmpty())  proceed = false
        if (proceed) {
            if (chatRoomUid.isNullOrEmpty()) { //채팅방이 없다면
                sendButton.isEnabled = false // 파이어베이스 서버와 접속시 비동기 이므로 접속이 될 때까지 버튼이 몇 번 눌러짐. 막기위해 false
                //채팅방이 만들어지면 참여하는 2명의 유저에 대한 uid를 넣어주기
                val users = HashMap<String, Boolean>()
                users[userId.toString()] = true //나의 uid
                users[destinationUid.toString()] = true //상대방의 uid
                val chat = Chat()
                chat.users = users
                firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).push().setValue(chat) //채팅방생성
                    .addOnSuccessListener {
                        firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).orderByChild("users/${userId.toString()}").equalTo(true) //1.나의 uid 찾기
                            .addListenerForSingleValueEvent( object : ValueEventListener{
                                override fun onDataChange(data: DataSnapshot) {
                                    for (item in data.children) {
                                        val result = item.getValue(Chat::class.java)
                                        if (result?.users?.containsKey(destinationUid)!!){
                                            chatRoomUid = item.key
                                            writeMessage()
                                            messageET.text = null //텍스트창 초기화
                                        }
                                    }
                                }
                                override fun onCancelled(data: DatabaseError) {
                                    data.toException().printStackTrace()
                                }
                            })
                    }
            } else { //채팅방이 기존에 있다면
                sendButton.isEnabled = false
                writeMessage()
                messageET.text = null
            }
        }
    }
    
    /** 
     * 메세지 작성하기
     * */
    private fun writeMessage () {
        val comment = Chat.Comment()
        comment.uid = userId
        comment.message = messageET.text.toString()
        comment.timestamp = ServerValue.TIMESTAMP
        chatRoomUid?.let {
            firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).child(it).child(DATA_CHAT_COMMENTS).push().setValue(comment)
                .addOnSuccessListener {
                    updateChatList()
                    sendButton.isEnabled = true //전송버튼 다시 누를 수 있게 하기
                }
                .addOnFailureListener {
                    it.printStackTrace() //채팅방 생성 실패
                }
        }
        sendFcmPush(destinationToken) //푸쉬알림 보내기
    }

    /**
     * 상대방 정보 알아내기
     * */
    private fun updateChatList() {
        var user: User
        firebaseRealtimeDB.child(DATA_USERS).child(destinationUid!!).addListenerForSingleValueEvent(object : ValueEventListener { //상대방정보 알아내기
            override fun onDataChange(data: DataSnapshot) {
                data?.let {
                    user = data.getValue(User::class.java)!!
                    getMessageList(user)
                }
            }
            override fun onCancelled(data: DatabaseError) {
                data.toException().printStackTrace()
            }
        })
    }

    /**
     * 채팅방 메시지리스트 ChatListAdapter로 보내기
     * 메시지 읽음표시 기능
     * */
    fun getMessageList(user: User?) {
        if (comments == null) {
            comments = arrayListOf<Chat.Comment>()
        }
        databaseReference = firebaseRealtimeDB.child(DATA_CHAT_CHATROOMS).child(chatRoomUid!!).child(DATA_CHAT_COMMENTS)
        valueEventListener = databaseReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(data: DataSnapshot) {
                for (document in data.children) {
                    val key = document.key
                    Log.d("key확인", "키값은 : $key") //계속 호출 되는중
                    val comment = document.getValue(Chat.Comment::class.java)
                    comments?.add(comment!!)
                }
                comments?.let {
                    chatListAdapter?.updateChats(it, user!!, chatRoomUid!!)
                    chatRoomList.scrollToPosition(it.size - 1) //채팅 입력되면 화면 제일 아래로 내려가기
                    it.clear() //반드시 clear 해줘야함. 안 그러면 heap 영역에 있기때문에 데이터 중복으로 쌓임, ChatListAdpater에서 해주는 notifyDataSetChanged는 리스트에 보여줄게 있다는 걸 알려주는 것이지, 데이터 중복까지 없애주는 건 아님
                }
            }
            override fun onCancelled(data: DatabaseError) {
                data.toException().printStackTrace()
            }
        })
    }
    
    /**
     * 푸쉬 보내기
     * */
    private fun sendFcmPush(targetToken: String?) {
        
        //백그라운드용 푸쉬
        val backgroundPushData = NotificationContent()
        //notificationContent.title = myNickname
        backgroundPushData.title = FirebaseAuth.getInstance().currentUser?.displayName
        backgroundPushData.text = messageET.text.toString()

        //포그라운드용 푸쉬
        val foregroundPushData = Data()
        foregroundPushData.title = FirebaseAuth.getInstance().currentUser?.displayName
        foregroundPushData.text = messageET.text.toString()

        val notificationModel = NotificationModel()
        notificationModel.notification = backgroundPushData
        notificationModel.data = foregroundPushData
        notificationModel.to = targetToken //상대방의 pushToken

        val retrofit = PushFcmApiService().getApi()

        //retrofit 객체를 통해 인터페이스 생성
        val service = retrofit?.create(PushFcmApi::class.java)

        service?.postFcmRequest(notificationModel)?.enqueue(object: Callback<ContactsContract.RawContacts.Data>{
            @SuppressLint("LongLogTag")
            override fun onResponse(call: Call<ContactsContract.RawContacts.Data>, response: Response<ContactsContract.RawContacts.Data>) {
                Log.d(PUSH_CALLBACK_RESULT_SUCCESS, SUCCESS)
            }

            @SuppressLint("LongLogTag")
            override fun onFailure(call: Call<ContactsContract.RawContacts.Data>, t: Throwable) {
                Log.d(PUSH_CALLBACK_RESULT_FAILURE, FAILURE)
                t.printStackTrace()
            }
        })
    }

    /**
     * 나가기 버튼 눌렀을 때 database-watching 끄기
     * */
    override fun onBackPressed() {
        //super.onBackPressed()
        finish()
        databaseReference?.removeEventListener(valueEventListener!!)
        overridePendingTransition(R.anim.fromleft, R.anim.toright) //종료시 왼쪽으로 닫히는 애니메이션
    }

}
