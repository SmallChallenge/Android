package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommunityFragment : Fragment() {

    private lateinit var rvCommunity: RecyclerView
    private lateinit var emptyStateContainer: ConstraintLayout
    private lateinit var btnProfile: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        rvCommunity.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Adapter 설정
        // rvCommunity.adapter = communityAdapter
    }

    /**
     * 커뮤니티 게시물 로드
     */
    private fun loadCommunityPosts() {
        // TODO: 서버에서 커뮤니티 데이터 로드
        // 현재는 빈 상태 표시

        // 임시로 빈 상태 표시
        showEmptyState()

        // 추후 데이터가 있으면:
        // if (posts.isEmpty()) {
        //     showEmptyState()
        // } else {
        //     hideEmptyState()
        //     communityAdapter.setPosts(posts)
        // }
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