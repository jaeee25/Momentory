package com.example.momentory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ItemSharedPostBinding

class PostAdapter(
    private val postList: List<Post>,
    private val viewType: Int,
    private val onPostClick: (Post, Int) -> Unit
) : RecyclerView.Adapter<PostAdapter.SharedPostViewHolder>() {

    companion object {
        const val VIEW_TYPE_SHARED = 1
        const val VIEW_TYPE_SECRET = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedPostViewHolder {
        val binding = ItemSharedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SharedPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SharedPostViewHolder, position: Int) {
        holder.bind(postList[position])
        holder.itemView.setOnClickListener { onPostClick(postList[position], position) }
    }

    override fun getItemCount(): Int = postList.size

    inner class SharedPostViewHolder(private val binding: ItemSharedPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postTitle.text = post.title
            binding.postDate.text = post.date
            binding.postAuthor.text = post.author
            binding.postContent.text = post.content
            binding.likeCount.text = post.likeCount.toString()
            binding.commentCount.text = post.commentCount.toString()
        }
    }
}
