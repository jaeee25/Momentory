package com.example.momentory

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentory.databinding.ActivityCreateCapsuleBinding
import com.example.momentory.databinding.ActivityProfileBinding
import com.example.momentory.databinding.TimecapsuleFriendsBinding


class FriendViewHolder(val binding: TimecapsuleFriendsBinding) :
    RecyclerView.ViewHolder(binding.root)

class FriendAdapter(
    private val names: MutableList<String>,
    private val isChecked: MutableList<Boolean>,
    private val onCheckedChange: (String, Boolean) -> Unit
) : RecyclerView.Adapter<FriendViewHolder>() {

    override fun getItemCount(): Int = names.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder =
        FriendViewHolder(
            TimecapsuleFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val name = names[position]
        holder.binding.friendName.text = name
        holder.binding.friendCheckBox.isChecked = isChecked[position]

        holder.binding.friendCheckBox.setOnCheckedChangeListener { _, isCheckedNow ->
            isChecked[position] = isCheckedNow
            onCheckedChange(name, isCheckedNow)
        }
    }

    fun updateData(newNames: MutableList<String>, newChecked: MutableList<Boolean>) {
        names.clear()
        names.addAll(newNames)
        isChecked.clear()
        isChecked.addAll(newChecked)
        notifyDataSetChanged()
    }
}

class CreateCapsuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCreateCapsuleBinding by lazy {
            ActivityCreateCapsuleBinding.inflate(layoutInflater)
        }
        enableEdgeToEdge()
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val friends = MutableList(20) { i -> "친구 ${i + 1}" }
        val isChecked = MutableList(20) { false }
        val selectedFriends = mutableListOf<String>()

        val adapter = FriendAdapter(friends, isChecked) { name, isCheckedNow ->
            if (isCheckedNow) {
                selectedFriends.add(name)
            } else {
                selectedFriends.remove(name)
            }
        }

        binding.timeCapsuleRecyclerView.layoutManager =
            LinearLayoutManager(this@CreateCapsuleActivity)
        binding.timeCapsuleRecyclerView.adapter = adapter

        binding.searchFriend.setOnClickListener {
            val query = binding.capsuleFriendName.text.toString()
            val filteredFriends = if (query.isEmpty()) {
                friends
            } else {
                friends.filter { it.contains(query) }.toMutableList()
            }
            val filteredChecked = filteredFriends.map { friends.indexOf(it) }
                .map { isChecked[it] }
                .toMutableList()

            adapter.updateData(filteredFriends, filteredChecked)
        }


        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            try {
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!, 230, 230
                )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio

                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null
                bitmap?.let {
                    binding.capsuleImage.setImageBitmap(bitmap)
                } ?: let {
                    Log.d("kkang", "bitmap null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.capsuleImage.setOnClickListener() {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
