package com.project.stampy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    // 가운데 버튼은 FAB로 처리하므로 여기서는 false 반환
                    false
                }
                R.id.navigation_community -> {
                    loadFragment(CommunityFragment())
                    true
                }
                else -> false
            }
        }

        // 플로팅 액션 버튼 클릭 리스너
        fabAdd.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun openCamera() {
        // 나중에 커스텀 카메라 Activity로 변경할 예정
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "카메라를 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
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
                Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}