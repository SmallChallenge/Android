package com.project.stampy

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * 갤러리 사진 그리드 어댑터
 */
class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private val photos = mutableListOf<Uri>()
    private var onPhotoClickListener: ((Uri) -> Unit)? = null

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_gallery_photo)

        fun bind(uri: Uri) {
            Glide.with(itemView.context)
                .load(uri)
                .centerCrop()
                .into(imageView)

            itemView.setOnClickListener {
                onPhotoClickListener?.invoke(uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_photo, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount() = photos.size

    fun setPhotos(newPhotos: List<Uri>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    fun setOnPhotoClickListener(listener: (Uri) -> Unit) {
        onPhotoClickListener = listener
    }
}