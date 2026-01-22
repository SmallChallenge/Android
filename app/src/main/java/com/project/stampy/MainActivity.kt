package com.project.stampy

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.stampy.auth.LoginActivity
import com.project.stampy.community.CommunityFragment
import com.project.stampy.data.local.NonLoginPhotoManager
import com.project.stampy.data.local.TokenManager
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: ImageView

    // 비로그인 유저 관리
    private lateinit var tokenManager: TokenManager
    private lateinit var nonLoginPhotoManager: NonLoginPhotoManager

    // 스플래시 화면 유지 플래그
    private var keepSplashScreen = true

    // 앰플리튜드
    private lateinit var amplitude: Amplitude

    // 뒤로가기 두 번 눌러 종료
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL: Long = 2000 // 2초

    companion object {
        private const val TAG = "MainActivity"

        // 커뮤니티 탭으로 이동할지 여부
        const val EXTRA_NAVIGATE_TO_COMMUNITY = "extra_navigate_to_community"

        // 내 기록 탭으로 이동할지 여부
        const val EXTRA_NAVIGATE_TO_STORAGE = "extra_navigate_to_storage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 스크린 설치 (super.onCreate 전에 호출)
        val splashScreen = installSplashScreen()

        // 스플래시 화면을 0.2초 동안 유지
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        lifecycleScope.launch {
            delay(200) // ( 100 = 0.1초, 1500 = 1.5초, 2000 = 2초)
            keepSplashScreen = false
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 상태바 검정색으로 설정(themes에서 설정했지만 혹시 모르니 보험..)
        window.statusBarColor = Color.parseColor("#171717")

        // ActionBar 숨기기(themes에서 설정했지만 혹시 모르니 보험..)
        supportActionBar?.hide()

        // 초기화
        tokenManager = TokenManager(this)
        nonLoginPhotoManager = NonLoginPhotoManager(this)

        bottomNav = findViewById(R.id.bottom_navigation)
        fabAdd = findViewById(R.id.fab_add)

        // Intent 확인하여 커뮤니티 탭으로 이동할지 결정
        val shouldNavigateToCommunity = intent.getBooleanExtra(EXTRA_NAVIGATE_TO_COMMUNITY, false)

        // 앱 시작시 기본 프래그먼트 설정
        if (savedInstanceState == null) {
            if (shouldNavigateToCommunity) {
                // 커뮤니티 탭으로 이동
                Log.d(TAG, "커뮤니티 탭으로 이동")
                loadFragment(CommunityFragment())
                bottomNav.selectedItemId = R.id.navigation_community
            } else {
                // 내 기록 프래그먼트를 기본으로 표시
                loadFragment(MyRecordsFragment())
                bottomNav.selectedItemId = R.id.navigation_storage
            }
        }

        // 하단 네비게이션 리스너
        bottomNav.setOnItemSelectedListener { item ->
            val commonProps = mapOf(
                "is_logged_in" to tokenManager.isLoggedIn(),
                "platform" to "android",
                "app_version" to "1.0.0"
            )

            when(item.itemId) {
                R.id.navigation_storage -> {
                    loadFragment(MyRecordsFragment())
                    // amplitude.track("storage_view_enter", commonProps)
                    true
                }
                R.id.navigation_add -> false
                R.id.navigation_community -> {
                    loadFragment(CommunityFragment())
                    amplitude.track("community_view_enter", commonProps)
                    true
                }
                else -> false
            }
        }

        // 앰플리튜드 초기화
        amplitude = Amplitude(
            Configuration(
                apiKey = getString(R.string.amplitude_api_key),
                context = applicationContext
            )
        )

        if (tokenManager.isLoggedIn()) {
            val savedUserId = tokenManager.getUserId()
            if (savedUserId != -1L) {
                amplitude.setUserId("user_$savedUserId")
            }
        }

        // 플로팅 액션 버튼 클릭 리스너 (비회원 20장 제한 체크)
        setupFabListener()

        // 구글 애드몹
        MobileAds.initialize(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // 새로운 Intent 확인하여 커뮤니티 탭으로 이동
        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_COMMUNITY, false)) {
            Log.d(TAG, "onNewIntent: 커뮤니티 탭으로 이동")
            loadFragment(CommunityFragment())
            bottomNav.selectedItemId = R.id.navigation_community
        }

        // 내 기록 탭으로 이동
        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_STORAGE, false)) {
            Log.d(TAG, "onNewIntent: 내 기록 탭으로 이동")
            loadFragment(MyRecordsFragment())
            bottomNav.selectedItemId = R.id.navigation_storage
        }
    }

    /**
     * 뒤로가기 두 번 눌러 종료
     */
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime <= BACK_PRESS_INTERVAL) {
            // 2초 이내에 뒤로가기를 다시 누르면 앱 종료
            super.onBackPressed()
            finishAffinity() // 모든 액티비티 종료
        } else {
            // 첫 번째 뒤로가기
            backPressedTime = System.currentTimeMillis()
            showToast("한 번 더 누르면 종료됩니다.")
        }
    }

    /**
     * FAB 클릭 리스너 (20장 제한 체크 포함)
     */
    private fun setupFabListener() {
        fabAdd.setOnClickListener {
            // 비로그인 유저 20장 제한 체크
            if (!tokenManager.isLoggedIn()) {
                if (!nonLoginPhotoManager.canSaveMorePhotos()) {
                    showPhotoLimitDialog()
                    return@setOnClickListener
                }
            }

            // 20장 미만이거나 로그인 유저면 카메라로 이동
            navigateToCamera()
        }
    }

    /**
     * 20장 도달 시 로그인 유도 모달
     */
    private fun showPhotoLimitDialog() {
        // 한도 모달 로그 전송
        amplitude.track("view_limit_dialog", mapOf(
            "is_logged_in" to false,
            "platform" to "android"
        ))

        DoubleButtonDialog(this)
            .setTitle("기록 한도에 도달했어요.\n로그인하면 계속 기록할 수 있어요.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("로그인")
            .setOnCancelListener {
                Log.d(TAG, "20장 제한 모달 - 취소")
            }
            .setOnConfirmListener {
                Log.d(TAG, "20장 제한 모달 - 로그인")
                navigateToLogin()
            }
            .show()
    }

    /**
     * 로그인 화면으로 이동
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * 카메라 화면으로 이동
     */
    private fun navigateToCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                showToast("사진 촬영을 위해 \n카메라 접근 권한을 허용해 주세요.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 카메라에서 돌아왔을 때 프래그먼트 새로고침
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // PhotoSaveActivity에서 돌아온 경우 항상 MyRecordsFragment로 전환
        val shouldNavigateToStorage = intent.getBooleanExtra(EXTRA_NAVIGATE_TO_STORAGE, false)

        if (shouldNavigateToStorage) {
            // 플래그 제거
            intent.removeExtra(EXTRA_NAVIGATE_TO_STORAGE)

            // 내 기록 탭으로 이동
            loadFragment(MyRecordsFragment())
            bottomNav.selectedItemId = R.id.navigation_storage
        } else {
            // 현재 프래그먼트 새로고침
            if (currentFragment is MyRecordsFragment) {
                currentFragment.refreshPhotos()
            } else if (currentFragment is CommunityFragment) {
                currentFragment.refreshCommunity()
            }
        }
    }
}