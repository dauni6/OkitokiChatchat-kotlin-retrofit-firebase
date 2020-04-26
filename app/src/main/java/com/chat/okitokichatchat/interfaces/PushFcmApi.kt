package com.chat.okitokichatchat.interfaces

import android.provider.ContactsContract
import com.chat.okitokichatchat.util.NotificationModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PushFcmApi {

    //post하기
    @Headers("Content-Type: application/json", "Authorization: key=AAAAxH0SGMs:APA91bFFENh4AvuBeEGItF9Eecp6H8NZKQfFMwW9g_RkVbSIkAJbH9qg6lpNl7r98WzwOjJpHktAYLX1P78zkr7as52xCXbZStEoDdwIIboJlYur2fKFtSXsAYctKFQ9esSZ4FTo0PhO")
    @POST("fcm/send")
    fun postFcmRequest(@Body data: NotificationModel) : Call<ContactsContract.RawContacts.Data>

}