package com.chat.okitokichatchat.interfaces

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PushFcmApiService {
    private val BASE_URL = "https://fcm.googleapis.com/" //https://fcm.googleapis.com/fcm/send <- full url
    private var retrofit: Retrofit? = null
    //retrofit 객체 생성하기
    fun getApi(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

}