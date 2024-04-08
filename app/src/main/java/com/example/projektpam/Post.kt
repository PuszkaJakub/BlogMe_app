package com.example.projektpam

import com.google.firebase.Timestamp

data class Post(
    var author: String = "",
    var content: String = "",
    var time: Timestamp = Timestamp.now(),
    var recipient: String = ""
)