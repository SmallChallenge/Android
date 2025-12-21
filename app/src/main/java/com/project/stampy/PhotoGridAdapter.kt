package com.project.stampy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.project.stampy.data.model.Photo

/**
 * 사진 그리드 어댑터
 */
class PhotoGridAdapter : RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder>() {

    private val photos = mutableListOf<Photo>()
    private var onPhotoClickListener: ((Photo) -> Unit)? = null

    fun setPhotos(newPhotos: List<Photo>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    fun setOnPhotoClickListener(listener: (Photo) -> Unit) {
        onPhotoClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)

        fun bind(photo: Photo) {
            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(photo.file)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(ivPhoto)

            // 클릭 리스너
            itemView.setOnClickListener {
                onPhotoClickListener?.invoke(photo)
            }
        }
    }
}