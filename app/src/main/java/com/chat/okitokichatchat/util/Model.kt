package com.chat.okitokichatchat.util

data class User (
    val userName: String? = "",
    val imageUri: String? = "",
    val userId: String? = "",
    val pushToken: String? = "",
    val comment: String? = ""

)

data class Chat(
    var users: Map<String, Boolean>? = HashMap(),
    var comments: Map<String, Comment>? = HashMap() ) {

    class Comment {
        var uid: String? = ""
        var message: String? = ""
        var timestamp: Any? = ""
        var readUsers = HashMap<String, Any>()
    }
}

data class NotificationModel(
    var to: String? = "",
    var notification: NotificationContent? = null,
    var data: Data? = null
)

data class NotificationContent(
    var title: String? = "",
    var text: String? = ""
)

data class Data(
    var title: String? = "",
    var text: String? = ""
)