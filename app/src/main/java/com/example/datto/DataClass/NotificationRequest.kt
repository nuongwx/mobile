package com.example.datto.DataClass

data class MessageRequest(
    var message: NotificationRequest,
)

data class NotificationRequest(
    var topic: String,
    var sendAt: String, // Assuming the format is yyyy-MM-ddTHH:mm:ss
    var notification: Notification,
)

data class Notification(
    var title: String,
    var body: String,
)

data class GroupInfo(
    var groupId: String,
    var groupName: String,
)
