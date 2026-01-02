package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.FeedItem
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.CommunityRepository
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private lateinit var rvCommunity: RecyclerView
    private lateinit var emptyStateContainer: ConstraintLayout
    private lateinit var btnProfile: ImageView

    private lateinit var communityAdapter: CommunityFeedAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var communityRepository: CommunityRepository

    // 페이징 관련
    private var isLoading = false
    private var hasNext = true
    private var lastPublishedAt: String? = null
    private var lastImageId: Long? = null

    companion object {
        private const val TAG = "CommunityFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기화
        tokenManager = TokenManager(requireContext())
        RetrofitClient.initialize(tokenManager)
        communityRepository = CommunityRepository(tokenManager)

        // View 초기화
        initViews(view)

        // 프로필 버튼 클릭 리스너
        setupProfileButton()

        // RecyclerView 설정
        setupRecyclerView()

        // 커뮤니티 데이터 로드
        loadCommunityPosts()
    }

    private fun initViews(view: View) {
        rvCommunity = view.findViewById(R.id.rv_community)
        emptyStateContainer = view.findViewById(R.id.empty_state_container)
        btnProfile = view.findViewById(R.id.btn_profile)
    }

    /**
     * 프로필 버튼 클릭 리스너
     */
    private fun setupProfileButton() {
        btnProfile.setOnClickListener {
            val intent = Intent(requireContext(), MyPageActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        communityAdapter = CommunityFeedAdapter()

        rvCommunity.layoutManager = LinearLayoutManager(requireContext())
        rvCommunity.adapter = communityAdapter

        // 좋아요 클릭 리스너
        communityAdapter.setOnLikeClickListener { feed, position ->
            toggleLike(feed)
        }

        // 메뉴 클릭 리스너 (신고하기)
        communityAdapter.setOnMenuClickListener { feed ->
            showToast("신고 기능은 준비 중입니다")
            Log.d(TAG, "신고하기 클릭: imageId=${feed.imageId}")
            // TODO: 신고 기능 구현
        }

        // 스크롤 리스너 (페이징)
        rvCommunity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // 마지막 아이템에 도달하면 다음 페이지 로드
                if (!isLoading && hasNext) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        loadMorePosts()
                    }
                }
            }
        })
    }

    /**
     * 커뮤니티 게시물 로드 (첫 페이지)
     */
    private fun loadCommunityPosts() {
        if (isLoading) return
        isLoading = true

        // 페이징 초기화
        lastPublishedAt = null
        lastImageId = null
        hasNext = true

        viewLifecycleOwner.lifecycleScope.launch {
            communityRepository.getCommunityFeeds(
                category = null, // 전체 카테고리
                lastPublishedAt = null,
                lastImageId = null,
                size = 20,
                sort = "LATEST"
            ).onSuccess { response ->
                Log.d(TAG, "커뮤니티 피드 ${response.feeds.size}개 로드됨")

                if (response.feeds.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    communityAdapter.setFeeds(response.feeds)

                    // 페이징 정보 업데이트
                    hasNext = response.sliceInfo.hasNext
                    lastPublishedAt = response.sliceInfo.nextCursorPublishedAt
                    lastImageId = response.sliceInfo.nextCursorId
                }
            }.onFailure { error ->
                Log.e(TAG, "커뮤니티 피드 로드 실패", error)
                showEmptyState()
            }

            isLoading = false
        }
    }

    /**
     * 추가 게시물 로드 (페이징)
     */
    private fun loadMorePosts() {
        if (isLoading || !hasNext) return
        isLoading = true

        Log.d(TAG, "다음 페이지 로드: lastPublishedAt=$lastPublishedAt, lastImageId=$lastImageId")

        viewLifecycleOwner.lifecycleScope.launch {
            communityRepository.getCommunityFeeds(
                category = null,
                lastPublishedAt = lastPublishedAt,
                lastImageId = lastImageId,
                size = 20,
                sort = "LATEST"
            ).onSuccess { response ->
                Log.d(TAG, "추가 피드 ${response.feeds.size}개 로드됨")

                if (response.feeds.isNotEmpty()) {
                    communityAdapter.addFeeds(response.feeds)

                    // 페이징 정보 업데이트
                    hasNext = response.sliceInfo.hasNext
                    lastPublishedAt = response.sliceInfo.nextCursorPublishedAt
                    lastImageId = response.sliceInfo.nextCursorId
                }
            }.onFailure { error ->
                Log.e(TAG, "추가 피드 로드 실패", error)
            }

            isLoading = false
        }
    }

    /**
     * 좋아요 토글
     */
    private fun toggleLike(feed: FeedItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            // 낙관적 업데이트 (즉시 UI 변경)
            val newIsLiked = !feed.isLiked
            val newLikeCount = if (newIsLiked) feed.likeCount + 1 else feed.likeCount - 1

            communityAdapter.updateLikeStatus(
                imageId = feed.imageId,
                isLiked = newIsLiked,
                likeCount = newLikeCount
            )

            // 서버 요청
            communityRepository.toggleLike(feed.imageId)
                .onSuccess { response ->
                    Log.d(TAG, "좋아요 토글 성공: imageId=${feed.imageId}, isLiked=${response.isLiked}")

                    // 서버 응답으로 최종 업데이트
                    val finalLikeCount = if (response.isLiked) {
                        if (feed.isLiked) feed.likeCount else feed.likeCount + 1
                    } else {
                        if (feed.isLiked) feed.likeCount - 1 else feed.likeCount
                    }

                    communityAdapter.updateLikeStatus(
                        imageId = feed.imageId,
                        isLiked = response.isLiked,
                        likeCount = finalLikeCount
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "좋아요 토글 실패", error)

                    // 실패 시 원래 상태로 복원
                    communityAdapter.updateLikeStatus(
                        imageId = feed.imageId,
                        isLiked = feed.isLiked,
                        likeCount = feed.likeCount
                    )

                    showToast("좋아요에 실패했어요")
                }
        }
    }

    /**
     * 빈 상태 표시
     */
    private fun showEmptyState() {
        rvCommunity.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE
    }

    /**
     * 빈 상태 숨김
     */
    private fun hideEmptyState() {
        rvCommunity.visibility = View.VISIBLE
        emptyStateContainer.visibility = View.GONE
    }

    /**
     * 커뮤니티 새로고침 (MainActivity에서 호출 가능)
     */
    fun refreshCommunity() {
        loadCommunityPosts()
    }
}