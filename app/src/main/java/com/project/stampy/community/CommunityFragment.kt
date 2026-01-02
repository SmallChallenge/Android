package com.project.stampy.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.auth.LoginActivity
import com.project.stampy.auth.MyPageActivity
import com.project.stampy.R
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.FeedItem
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.CommunityRepository
import com.project.stampy.etc.DoubleButtonDialog
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

    // 로그인 결과 처리
    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // 로그인 성공 - 커뮤니티 새로고침
            Log.d(TAG, "로그인 성공 - 커뮤니티 새로고침")
            refreshCommunity()
        }
    }

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

    override fun onResume() {
        super.onResume()
        // 이미 데이터가 로드된 상태라면 새로고침
        if (communityAdapter.itemCount > 0) {
            Log.d(TAG, "onResume: 커뮤니티 데이터 새로고침")
            refreshCommunity()
        }
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
            // 비로그인 상태 체크
            if (!tokenManager.isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                toggleLike(feed)
            }
        }

        // 메뉴 클릭 리스너 (신고하기)
        communityAdapter.setOnMenuClickListener { feed ->
            // 비로그인 상태 체크
            if (!tokenManager.isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                showReportDialog(feed)
            }
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
     * 로그인 필요 다이얼로그 표시
     */
    private fun showLoginRequiredDialog() {
        DoubleButtonDialog(requireContext())
            .setTitle("로그인이 필요해요.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("로그인")
            .setOnCancelListener {
                Log.d(TAG, "로그인 취소")
            }
            .setOnConfirmListener {
                Log.d(TAG, "로그인 화면으로 이동")
                navigateToLogin()
            }
            .show()
    }

    /**
     * 로그인 화면으로 이동
     */
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            // 커뮤니티에서 왔다는 정보 전달
            putExtra(LoginActivity.EXTRA_RETURN_TO_COMMUNITY, true)
        }
        loginLauncher.launch(intent)
    }

    /**
     * 신고 확인 다이얼로그 표시
     */
    private fun showReportDialog(feed: FeedItem) {
        DoubleButtonDialog(requireContext())
            .setTitle("부적절한 게시물인가요?")
            .setCancelButtonText("취소")
            .setConfirmButtonText("신고")
            .setOnCancelListener {
                // 취소 시 아무 동작 없음
            }
            .setOnConfirmListener {
                reportPost(feed)
            }
            .show()
    }

    /**
     * 게시물 신고
     */
    private fun reportPost(feed: FeedItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            communityRepository.reportPost(feed.imageId)
                .onSuccess { response ->
                    Log.d(TAG, "신고 성공: imageId=${feed.imageId}, reportedAt=${response.reportedAt}")
                    showToast("신고가 접수되었어요.")
                }
                .onFailure { error ->
                    Log.e(TAG, "신고 실패: ${error.message}", error)

                    val errorMsg = error.message ?: ""

                    // 에러 메시지 처리
                    val toastMessage = when {
                        // 자신의 게시물 신고 시도 (메시지에 "자신" 포함)
                        errorMsg.contains("SELF_REPORT_NOT_ALLOWED") ||
                                errorMsg.contains("자신의 게시물") ->
                            "본인 게시물은 신고할 수 없어요."

                        // 이미 신고한 게시물
                        errorMsg.contains("DUPLICATE_REPORT") ||
                                errorMsg.contains("이미") ->
                            "이미 신고한 게시물입니다"

                        // 기타 에러
                        else ->
                            "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."
                    }

                    showToast(toastMessage)
                }
        }
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
                category = null,
                lastPublishedAt = null,
                lastImageId = null,
                size = 20,
                sort = "LATEST"
            ).onSuccess { response ->
                Log.d(TAG, "커뮤니티 피드 ${response.feeds.size}개 로드됨")

                // 디버깅: 좋아요 상태 로그
                response.feeds.forEach { feed ->
                    Log.d(TAG, "imageId=${feed.imageId}, liked=${feed.isLiked}, likeCount=${feed.likeCount}")
                }

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
                    communityAdapter.updateLikeStatus(
                        imageId = feed.imageId,
                        isLiked = response.isLiked,
                        likeCount = newLikeCount
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