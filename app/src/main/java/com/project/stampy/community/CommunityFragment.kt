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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

        // SwipeRefreshLayout 설정
        setupSwipeRefresh()

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
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
    }

    /**
     * SwipeRefreshLayout 설정
     */
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Pull-to-Refresh 트리거됨")
            refreshCommunity()
        }

        // 로딩 인디케이터 색상 설정
        swipeRefreshLayout.setColorSchemeResources(
            R.color.neon_600,
            R.color.neon_500,
            R.color.neon_400
        )

        // 프로그레스 뷰의 시작/끝 오프셋 설정 (dp를 px로 변환)
        val displayMetrics = resources.displayMetrics
        val startOffset = (0 * displayMetrics.density).toInt() // 시작 위치 (0dp)
        val endOffset = (120 * displayMetrics.density).toInt() // 끝 위치 (120dp) - 스와이프 거리

        swipeRefreshLayout.setProgressViewOffset(
            false, // scale 애니메이션 사용 안함
            startOffset,
            endOffset
        )

        // 프로그레스 뷰 끝까지 당겨야 새로고침되도록 설정
        swipeRefreshLayout.setDistanceToTriggerSync(endOffset)
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

        // 신고 클릭 리스너
        communityAdapter.setOnReportClickListener { feed ->
            // 비로그인 상태 체크
            if (!tokenManager.isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                showReportDialog(feed)
            }
        }

        // 차단 클릭 리스너
        communityAdapter.setOnBlockClickListener { feed ->
            // 비로그인 상태 체크
            if (!tokenManager.isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                handleBlockClick(feed)
            }
        }

        // 스크롤 리스너 (페이징 + 팝오버 닫기)
        rvCommunity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 스크롤 시 열려있는 팝오버 닫기
                if (dy != 0) {  // 실제로 스크롤이 발생했을 때만
                    communityAdapter.closeAllPopovers()
                }

                // 페이징 처리
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

                    // 신고 성공 시 커뮤니티 새로고침
                    refreshCommunity()
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
     * 차단 클릭 처리
     */
    private fun handleBlockClick(feed: FeedItem) {
        // 차단 확인 다이얼로그
        DoubleButtonDialog(requireContext())
            .setTitle("${feed.nickname}님을 차단하시겠습니까?")
            .setDescription("차단하면 이 사용자의 게시물이 더이상 표시되지 않습니다.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("차단")
            .setOnConfirmListener {
                performBlock(feed)
            }
            .show()
    }

    /**
     * 실제 차단 수행
     */
    private fun performBlock(feed: FeedItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            communityRepository.blockUser(feed.nickname)
                .onSuccess { response ->
                    Log.d(TAG, "차단 성공: nickname=${feed.nickname}, blockedAt=${response.blockedAt}")
                    showToast("게시자를 차단했습니다.")

                    // 차단된 사용자의 모든 게시물 제거
                    communityAdapter.removeBlockedUserPosts(feed.nickname)
                }
                .onFailure { error ->
                    Log.e(TAG, "차단 실패: ${error.message}", error)

                    val errorMsg = error.message ?: ""

                    // 에러 메시지 처리
                    val toastMessage = when {
                        // 본인 차단 시도
                        errorMsg.contains("SELF_BLOCK_NOT_ALLOWED") ||
                                errorMsg.contains("본인") ||
                                errorMsg.contains("자신") ->
                            "본인 게시물은 차단할 수 없어요."

                        // 이미 차단한 사용자
                        errorMsg.contains("DUPLICATE_BLOCK") ||
                                errorMsg.contains("이미") ->
                            "이미 차단한 사용자입니다."

                        // 기타 에러
                        else ->
                            "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."
                    }

                    showToast(toastMessage)
                }
        }
    }

    /**
     * 커뮤니티 게시물 로드 (첫 페이지 로딩 및 새로고침)
     */
    private fun loadCommunityPosts() {
        if (isLoading) return
        isLoading = true

        // 즉시 로딩 인디케이터를 활성화
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
        }

        // 페이징 초기화
        lastPublishedAt = null
        lastImageId = null
        hasNext = true

        viewLifecycleOwner.lifecycleScope.launch {
            // 로딩 시작 시간 기록
            val startTime = System.currentTimeMillis()

            communityRepository.getCommunityFeeds(
                category = null,
                lastPublishedAt = null,
                lastImageId = null,
                size = 20,
                sort = "LATEST"
            ).onSuccess { response ->
                Log.d(TAG, "커뮤니티 피드 ${response.feeds.size}개 로드됨")

                // 데이터 유무에 따른 UI 처리
                if (response.feeds.isEmpty()) {
                    showEmptyState()
                } else {
                    // 데이터가 있으면 RecyclerView만 표시
                    hideEmptyState()
                    communityAdapter.setFeeds(response.feeds)

                    // 페이징 정보 업데이트
                    hasNext = response.sliceInfo.hasNext
                    lastPublishedAt = response.sliceInfo.nextCursorPublishedAt
                    lastImageId = response.sliceInfo.nextCursorId
                }
            }.onFailure { error ->
                Log.e(TAG, "커뮤니티 피드 로드 실패", error)
                // 에러 발생 시에만 빈 화면 표시
                showEmptyState()
            }

            // 통신이 0.1초 만에 끝나더라도 사용자가 로딩을 인지할 수 있게 0.7초 정도 유지
            val minLoadingTime = 700L
            val elapsedTime = System.currentTimeMillis() - startTime

            if (elapsedTime < minLoadingTime) {
                kotlinx.coroutines.delay(minLoadingTime - elapsedTime)
            }

            // 로딩 상태 해제 및 인디케이터 숨기기
            isLoading = false
            swipeRefreshLayout.isRefreshing = false
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
     * 커뮤니티 새로고침 (MainActivity에서 호출 가능 + Pull-to-Refresh)
     */
    fun refreshCommunity() {
        Log.d(TAG, "커뮤니티 새로고침 시작")
        loadCommunityPosts()
    }
}