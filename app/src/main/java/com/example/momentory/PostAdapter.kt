package com.example.momentory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentory.databinding.ItemSharedPostBinding
import com.example.momentory.databinding.ItemSecretPostBinding

class PostAdapter(
    private val postList: List<Post>,
    private val viewType: Int,
    private val onPostClick: (Post, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SHARED = 1
        const val VIEW_TYPE_SECRET = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SHARED) {
            val binding = ItemSharedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SharedPostViewHolder(binding)
        } else {
            val binding = ItemSecretPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SecretPostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = postList[position]
        if (holder is SharedPostViewHolder) {
            holder.bind(post)
            holder.itemView.setOnClickListener { onPostClick(post, position) }
        } else if (holder is SecretPostViewHolder) {
            holder.bind(post)
            holder.itemView.setOnClickListener { onPostClick(post, position) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun getItemCount(): Int = postList.size

    inner class SharedPostViewHolder(private val binding: ItemSharedPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postTitle.text = post.title
            binding.postDate.text = post.date
            binding.postUser.text = post.user
            binding.postContent.text = post.content
            binding.likeCount.text = post.reactionTotal.toString()
            binding.commentCount.text = post.commentCount.toString()

            // 이미지 처리
            Glide.with(binding.root.context)
                .load(post.photoUrl) // 이미지 URL
                .placeholder(R.drawable.ic_sample_image) // 로딩 중 보여줄 이미지
                .error(R.drawable.ic_error_image) // 실패 시 보여줄 이미지
                .into(binding.postPhoto) // ImageView에 이미지 설정
        }
    }

    inner class SecretPostViewHolder(private val binding: ItemSecretPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postTitle.text = post.title
            binding.postDate.text = post.date
            binding.postContent.text = post.content

            // 이미지 처리
            Glide.with(binding.root.context)
                .load(post.photoUrl) // 이미지 URL
                .placeholder(R.drawable.ic_sample_image) // 로딩 중 보여줄 이미지
                .error(R.drawable.ic_error_image) // 실패 시 보여줄 이미지
                .into(binding.postPhoto) // ImageView에 이미지 설정
        }
    }

}