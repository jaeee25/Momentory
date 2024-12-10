package com.example.momentory

data class Post(
    val postId: String = "",
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
    var reactionTotal: Int = 0 // 리액션 합계 필드 추가
) {
    /**
     * 동적으로 리액션 합계를 계산하는 함수
     */
    fun calculateReactionTotal() {
        reactionTotal = reactions.values.sum() // 모든 리액션 값을 더해 합계를 설정
    }
}
