package com.example.momentory

data class Post(
    var id: String = "",
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
    ),
    var reactionTotal: Int = 0
) {
    fun calculateReactionTotal() {
        reactionTotal = reactions.values.sum()
    }
}
