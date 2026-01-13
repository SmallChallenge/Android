package com.project.stampy.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.stampy.R
import com.project.stampy.data.model.FeedItem

/**
 * 커뮤니티 피드 어댑터
 */
class CommunityFeedAdapter : RecyclerView.Adapter<CommunityFeedAdapter.FeedViewHolder>() {

    private val feeds = mutableListOf<FeedItem>()
    private var onLikeClickListener: ((FeedItem, Int) -> Unit)? = null
    private var onMenuClickListener: ((FeedItem) -> Unit)? = null

    // 현재 열려있는 팝오버의 ViewHolder 추적
    private var currentOpenPopoverHolder: FeedViewHolder? = null

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

    /**
     * 모든 팝오버 닫기 (외부에서 호출 가능)
     */
    fun closeAllPopovers() {
        currentOpenPopoverHolder?.hidePopover()
        currentOpenPopoverHolder = null
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        private val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
        private val btnMenu: FrameLayout = itemView.findViewById(R.id.btn_menu)
        private val popoverMenu: CardView = itemView.findViewById(R.id.popover_menu)
        private val menuReport: LinearLayout = itemView.findViewById(R.id.menu_report)
        private val ivFeedImage: ImageView = itemView.findViewById(R.id.iv_feed_image)
        private val btnLike: FrameLayout = itemView.findViewById(R.id.btn_like)
        private val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)

        fun bind(feed: FeedItem) {
            // 닉네임
            tvNickname.text = feed.nickname

            // 프로필 이미지
            ivProfile.setImageResource(R.drawable.ic_my_profile)

            // 피드 이미지
            Glide.with(itemView.context)
                .load(feed.accessUrl)
                .centerCrop()
                .into(ivFeedImage)

            // 좋아요 상태
            updateLikeUI(feed.isLiked)
            tvLikeCount.text = feed.likeCount.toString()

            // 팝오버 초기화 (닫힌 상태)
            popoverMenu.visibility = View.GONE

            // 아이템 전체 클릭 (다른 아이템 클릭 시 팝오버 닫기)
            itemView.setOnClickListener {
                // 다른 ViewHolder의 팝오버가 열려있으면 닫기
                if (currentOpenPopoverHolder != null && currentOpenPopoverHolder != this@FeedViewHolder) {
                    closeAllPopovers()
                }
                // 자신의 팝오버가 열려있으면 닫기
                else if (popoverMenu.visibility == View.VISIBLE) {
                    hidePopover()
                }
            }

            // 메뉴 버튼 클릭 (팝오버 토글)
            btnMenu.setOnClickListener {
                // 다른 팝오버가 열려있으면 먼저 닫기
                if (currentOpenPopoverHolder != null && currentOpenPopoverHolder != this@FeedViewHolder) {
                    closeAllPopovers()
                }
                togglePopover()
            }

            // 신고하기 클릭
            menuReport.setOnClickListener {
                hidePopover()
                onMenuClickListener?.invoke(feed)
            }

            // 좋아요 버튼 클릭
            btnLike.setOnClickListener {
                // 다른 팝오버가 열려있으면 먼저 닫기
                if (currentOpenPopoverHolder != null && currentOpenPopoverHolder != this@FeedViewHolder) {
                    closeAllPopovers()
                }
                onLikeClickListener?.invoke(feed, adapterPosition)
            }

            // 팝오버 자체 클릭 시 이벤트 전파 중지 (팝오버 내부 클릭 시 닫히지 않도록)
            popoverMenu.setOnClickListener {
                // 이벤트 전파 중지 (아무 동작 하지 않음)
            }
        }

        /**
         * 팝오버 토글
         */
        private fun togglePopover() {
            if (popoverMenu.visibility == View.VISIBLE) {
                hidePopover()
            } else {
                showPopover()
            }
        }

        /**
         * 팝오버 표시
         */
        private fun showPopover() {
            popoverMenu.visibility = View.VISIBLE
            currentOpenPopoverHolder = this
        }

        /**
         * 팝오버 숨김
         */
        fun hidePopover() {
            popoverMenu.visibility = View.GONE
            if (currentOpenPopoverHolder == this) {
                currentOpenPopoverHolder = null
            }
        }

        /**
         * 좋아요 UI 업데이트
         */
        private fun updateLikeUI(isLiked: Boolean) {
            if (isLiked) {
                // 좋아요 O: 채워진 하트 + neon_300 색상
                ivLike.setImageResource(R.drawable.ic_heart_filled)
                ivLike.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.neon_primary)
                )
            } else {
                // 좋아요 X: 빈 하트 + 흰색
                ivLike.setImageResource(R.drawable.ic_heart)
                ivLike.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
            }
        }
    }
}