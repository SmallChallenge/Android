package com.project.stampy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.stampy.data.local.NonLoginPhotoManager
import com.project.stampy.data.local.TokenManager
import com.project.stampy.ui.dialog.DoubleButtonDialog
import com.project.stampy.utils.showToast

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: ImageView

    // 비로그인 유저 관리
    private lateinit var tokenManager: TokenManager
    private lateinit var nonLoginPhotoManager: NonLoginPhotoManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 상태바 검정색으로 설정(themes에서 설정했지만 혹시 모르니 보험..)
        window.statusBarColor = Color.parseColor("#121212")

        // 초기화
        tokenManager = TokenManager(this)
        nonLoginPhotoManager = NonLoginPhotoManager(this)

        bottomNav = findViewById(R.id.bottom_navigation)
        fabAdd = findViewById(R.id.fab_add)

        // 앱 시작시 내 기록 프래그먼트를 기본으로 표시
        if (savedInstanceState == null) {
            loadFragment(MyRecordsFragment())
            bottomNav.selectedItemId = R.id.navigation_storage
        }

        // 하단 네비게이션 리스너
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_storage -> {
                    loadFragment(MyRecordsFragment())
                    true
                }
                R.id.navigation_add -> {
                    false
                }
                R.id.navigation_community -> {
                    loadFragment(CommunityFragment())
                    true
                }
                else -> false
            }
        }

        // 플로팅 액션 버튼 클릭 리스너 (비회원 20장 제한 체크)
        setupFabListener()
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
                showToast("카메라 권한이 필요합니다")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 카메라에서 돌아왔을 때 프래그먼트 새로고침
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is MyRecordsFragment) {
            currentFragment.refreshPhotos()
        }
    }
}