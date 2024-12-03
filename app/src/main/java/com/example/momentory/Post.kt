package com.example.momentory

data class Post(
    val title: String = "",
    val date: String = "",
    val author: String = "",
    val content: String = "",
    val imageUrl: String = "",
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
