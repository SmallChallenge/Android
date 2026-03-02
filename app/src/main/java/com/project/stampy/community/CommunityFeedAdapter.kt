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
 * 배너 아이템 모델
 */
data class BannerItem(
    val id: Int,
    val text1: String,
    val text2: String,
    val backgroundColorRes: Int,
    val isGuestBanner: Boolean
)

/**
 * 커뮤니티 피드 어댑터
 */
class CommunityFeedAdapter(
    private var isLoggedIn: Boolean,
    private val onLoginClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 실제 UI에 그려질 섞인 목록 (FeedItem + BannerItem)
    private val displayItems = mutableListOf<Any>()
    // 원본 피드 데이터만 보관
    private val originalFeeds = mutableListOf<FeedItem>()

    private var onLikeClickListener: ((FeedItem, Int) -> Unit)? = null
    private var onReportClickListener: ((FeedItem) -> Unit)? = null
    private var onBlockClickListener: ((FeedItem) -> Unit)? = null

    // 현재 열려있는 팝오버의 ViewHolder 추적
    private var currentOpenPopoverHolder: FeedViewHolder? = null

    companion object {
        private const val TYPE_FEED = 0
        private const val TYPE_BANNER = 1
        private const val BANNER_INTERVAL = 5 // 5개마다 배너 삽입

        // 비로그인용 배너 리스트
        private val GUEST_BANNERS = listOf(
            BannerItem(1, "나만의 갓생 기록도 자랑하고 싶다면?", "지금 로그인하고 첫 게시물을 올려보세요!", R.color.banner_orange, true),
            BannerItem(2, "로그인하고 마음에 드는 게시물에", "좋아요를 눌러 응원해 보세요!", R.color.banner_purple, true),
            BannerItem(3, "함께 기록하면 더 즐거워요.", "지금 로그인하고 커뮤니티를 즐겨보세요!", R.color.banner_blue, true),
            BannerItem(4, "이미 많은 분이 갓생을 기록 중이에요.", "로그인하고 스탬픽 메이트가 되어주세요!", R.color.banner_green, true)
        )

        // 로그인용 배너 리스트
        private val USER_BANNERS = listOf(
            BannerItem(5, "이미 많은 분들이 갓생을 기록 중이에요.", "여러분의 오늘 하루는 어땠나요?", R.color.banner_orange, false),
            BannerItem(6, "나만 보긴 아까운 갓생 기록들,", "로그인하고 친구들과 공유해 볼까요?", R.color.banner_green, false),
            BannerItem(7, "투박한 일상 사진도 스탬픽으로 예쁘게!", "지금 촬영 버튼을 눌러보세요", R.color.banner_blue, false),
            BannerItem(8, "오늘의 미션: 갓생 사진 1장 올리기!", "아직 늦지 않았어요, 지금 기록해 보세요.", R.color.banner_purple, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is BannerItem) TYPE_BANNER else TYPE_FEED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_BANNER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community_banner, parent, false)
            BannerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community_feed, parent, false)
            FeedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayItems[position]
        if (holder is FeedViewHolder && item is FeedItem) {
            holder.bind(item)
        } else if (holder is BannerViewHolder && item is BannerItem) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    /**
     * 피드와 배너를 섞어주는 로직
     */
    private fun updateDisplayItems() {
        displayItems.clear()
        val currentBanners = if (isLoggedIn) USER_BANNERS else GUEST_BANNERS
        var bannerIndex = 0

        originalFeeds.forEachIndexed { index, feedItem ->
            displayItems.add(feedItem)
            // 5번째 피드 뒤에 배너 추가 (1부터 시작할 때 5, 10, 15...)
            if ((index + 1) % BANNER_INTERVAL == 0) {
                displayItems.add(currentBanners[bannerIndex % currentBanners.size])
                bannerIndex++
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 피드 목록 설정
     */
    fun setFeeds(newFeeds: List<FeedItem>) {
        originalFeeds.clear()
        originalFeeds.addAll(newFeeds)
        updateDisplayItems()
    }

    /**
     * 피드 추가 (페이징)
     */
    fun addFeeds(newFeeds: List<FeedItem>) {
        originalFeeds.addAll(newFeeds)
        updateDisplayItems()
    }

    /**
     * 좋아요 상태 업데이트
     */
    fun updateLikeStatus(imageId: Long, isLiked: Boolean, likeCount: Int) {
        val position = originalFeeds.indexOfFirst { it.imageId == imageId }
        if (position != -1) {
            originalFeeds[position] = originalFeeds[position].copy(
                isLiked = isLiked,
                likeCount = likeCount
            )
            updateDisplayItems()
        }
    }

    /**
     * 차단된 사용자의 게시물 제거
     */
    fun removeBlockedUserPosts(nickname: String) {
        originalFeeds.removeAll { it.nickname == nickname }
        updateDisplayItems()
    }

    /**
     * 좋아요 클릭 리스너 설정
     */
    fun setOnLikeClickListener(listener: (FeedItem, Int) -> Unit) {
        onLikeClickListener = listener
    }

    /**
     * 신고 클릭 리스너 설정
     */
    fun setOnReportClickListener(listener: (FeedItem) -> Unit) {
        onReportClickListener = listener
    }

    /**
     * 차단 클릭 리스너 설정
     */
    fun setOnBlockClickListener(listener: (FeedItem) -> Unit) {
        onBlockClickListener = listener
    }

    /**
     * 모든 팝오버 닫기 (외부에서 호출 가능)
     */
    fun closeAllPopovers() {
        currentOpenPopoverHolder?.hidePopover()
        currentOpenPopoverHolder = null
    }

    /**
     * 배너 전용 ViewHolder
     */
    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: CardView = itemView.findViewById(R.id.banner_card)
        private val tvText1: TextView = itemView.findViewById(R.id.tv_banner_text1)
        private val tvText2: TextView = itemView.findViewById(R.id.tv_banner_text2)
        private val btnLogin: TextView = itemView.findViewById(R.id.btn_banner_login)
        private val ivArrow: ImageView = itemView.findViewById(R.id.iv_arrow)

        fun bind(banner: BannerItem) {
            tvText1.text = banner.text1
            tvText2.text = banner.text2
            card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, banner.backgroundColorRes))

            if (banner.isGuestBanner) {
                btnLogin.visibility = View.VISIBLE
                ivArrow.visibility = View.GONE
                btnLogin.setOnClickListener { onLoginClick?.invoke() }
            } else {
                btnLogin.visibility = View.GONE
                ivArrow.visibility = View.VISIBLE
                // 로그인 유저 클릭 로직 필요 시 추가
            }
        }
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        private val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
        private val btnMenu: FrameLayout = itemView.findViewById(R.id.btn_menu)
        private val popoverMenu: CardView = itemView.findViewById(R.id.popover_menu)
        private val menuReport: LinearLayout = itemView.findViewById(R.id.menu_report)
        private val menuBlock: LinearLayout = itemView.findViewById(R.id.menu_block)
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

            // 게시물 신고 클릭
            menuReport.setOnClickListener {
                hidePopover()
                onReportClickListener?.invoke(feed)
            }

            // 게시자 차단 클릭
            menuBlock.setOnClickListener {
                hidePopover()
                onBlockClickListener?.invoke(feed)
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