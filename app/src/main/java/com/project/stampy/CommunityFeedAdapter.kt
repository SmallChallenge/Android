package com.project.stampy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.stampy.data.model.FeedItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * 커뮤니티 피드 어댑터
 */
class CommunityFeedAdapter : RecyclerView.Adapter<CommunityFeedAdapter.FeedViewHolder>() {

    private val feeds = mutableListOf<FeedItem>()
    private var onLikeClickListener: ((FeedItem, Int) -> Unit)? = null
    private var onMenuClickListener: ((FeedItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(feeds[position])
    }

    override fun getItemCount(): Int = feeds.size

    /**
     * 피드 목록 설정
     */
    fun setFeeds(newFeeds: List<FeedItem>) {
        feeds.clear()
        feeds.addAll(newFeeds)
        notifyDataSetChanged()
    }

    /**
     * 피드 추가 (페이징)
     */
    fun addFeeds(newFeeds: List<FeedItem>) {
        val startPosition = feeds.size
        feeds.addAll(newFeeds)
        notifyItemRangeInserted(startPosition, newFeeds.size)
    }

    /**
     * 좋아요 상태 업데이트
     */
    fun updateLikeStatus(imageId: Long, isLiked: Boolean, likeCount: Int) {
        val position = feeds.indexOfFirst { it.imageId == imageId }
        if (position != -1) {
            feeds[position] = feeds[position].copy(
                isLiked = isLiked,
                likeCount = likeCount
            )
            notifyItemChanged(position)
        }
    }

    /**
     * 좋아요 클릭 리스너 설정
     */
    fun setOnLikeClickListener(listener: (FeedItem, Int) -> Unit) {
        onLikeClickListener = listener
    }

    /**
     * 메뉴 클릭 리스너 설정
     */
    fun setOnMenuClickListener(listener: (FeedItem) -> Unit) {
        onMenuClickListener = listener
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        private val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
        private val btnMenu: ImageView = itemView.findViewById(R.id.btn_menu)
        private val ivFeedImage: ImageView = itemView.findViewById(R.id.iv_feed_image)
        private val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)

        fun bind(feed: FeedItem) {
            // 닉네임
            tvNickname.text = feed.nickname

            // 프로필 이미지
            if (feed.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(feed.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.ic_profile)
            }

            // 피드 이미지
            Glide.with(itemView.context)
                .load(feed.accessUrl)
                .centerCrop()
                .into(ivFeedImage)

            // 좋아요 상태
            updateLikeUI(feed.isLiked)
            tvLikeCount.text = feed.likeCount.toString()

            // 좋아요 클릭
            ivLike.setOnClickListener {
                onLikeClickListener?.invoke(feed, adapterPosition)
            }

            // 메뉴 클릭
            btnMenu.setOnClickListener {
                onMenuClickListener?.invoke(feed)
            }
        }

        /**
         * 좋아요 UI 업데이트
         */
        private fun updateLikeUI(isLiked: Boolean) {
            val color = if (isLiked) {
                ContextCompat.getColor(itemView.context, R.color.neon_primary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.white)
            }
            ivLike.setColorFilter(color)
        }
    }
}