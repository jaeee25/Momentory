package com.example.momentory

data class Post(
    val title: String = "",
    val date: String = "",
    val user: String = "",
    val content: String = "",
    val photoUrl: String = "",
    val location: String = "",
    val weather: String = "",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var reactions: MutableMap<String, Int> = mutableMapOf(
        "😊" to 0,
        "😍" to 0,
        "❤️" to 0,
        "👍" to 0,
        "🔥" to 0
    )
)
