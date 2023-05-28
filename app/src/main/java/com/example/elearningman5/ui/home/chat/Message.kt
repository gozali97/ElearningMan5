package com.example.elearningman5.ui.home.chat

data class Message(
    var email:String,
    var name:String,
    var message:String,
    var receiver_role:String,
    var time:String)