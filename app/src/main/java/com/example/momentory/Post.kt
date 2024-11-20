package com.example.momentory


data class Post(
    val title: String,
    val date: String,
    val author: String,
    val content: String,
    val imageUrl: String,
    val likeCount: Int,
    val commentCount: Int
)