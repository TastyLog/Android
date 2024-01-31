package com.adam.tastylog.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.adam.tastylog.R
import com.adam.tastylog.databinding.ActivityMainBinding
import com.adam.tastylog.ui.fragment.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 Fragment 의 부모 Activity.
**/
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationServiceLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationServiceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (isLocationServiceEnabled()) {
                checkLocationPermission()
            }
        }

        handleLocationServiceAndPermission()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.menu_map) {
                handleLocationServiceAndPermission()
            }
            true
        }
    }

    private fun handleLocationServiceAndPermission() {
        if (isLocationServiceEnabled()) {
            checkLocationPermission()
        } else {
            showLocationServiceRequest()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMapFragment()
        }
    }

    private fun initMapFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, MapFragment())
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMapFragment()
            } else {
                explainPermissionRequirement()
            }
        }
    }

    private fun explainPermissionRequirement() {
        AlertDialog.Builder(this)
            .setTitle("위치 권한 필요")
            .setMessage("이 앱은 맵 기능을 사용하기 위해 위치 권한이 필요합니다. 위치 권한을 허용해 주세요.")
            .setPositiveButton("권한 요청") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationServiceRequest() {
        AlertDialog.Builder(this)
            .setTitle("위치 서비스 필요")
            .setMessage("이 앱은 위치 기반 서비스를 제공합니다. 위치 서비스를 활성화해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                locationServiceLauncher.launch(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}





//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        askNotificationPermission()
//        replaceFragment(MapFragment())  // 기본 맵 프래그먼트 표시
//
//        // UUID 생성
//        val uniqueID = UUID.randomUUID().toString()
//        Log.d("MyActivity", "Generated UUID: $uniqueID")
//
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.menu_map -> checkLocationPermission()  // Map 탭을 선택할 때마다 위치 권한 확인
//                else -> {}
//            }
//            true
//        }
//    }
//
//    private fun askNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
//                // 알림 권한이 이미 있으면 위치 권한 요청
//                checkLocationPermission()
//            } else {
//                // 알림 권한이 없으면 요청 후 위치 권한 요청은 권한 결과 콜백에서 진행
//                requestNotificationPermission()
//            }
//        } else {
//            // Android 13 미만은 위치 권한 요청 진행
//            checkLocationPermission()
//        }
//    }
//
//    private fun requestNotificationPermission() {
//        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), LOCATION_PERMISSION_REQUEST_CODE)
//    }
//
//    private fun checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                explainPermissionRequirement()
//            } else {
//                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//            }
//        } else {
//            initMapFragment()
//        }
//    }
//
//    private fun initMapFragment() {
//        replaceFragment(MapFragment())
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            LOCATION_PERMISSION_REQUEST_CODE -> {
//                // 권한 결과에 따라 알림/위치 권한 처리
//                if (permissions.contains(Manifest.permission.POST_NOTIFICATIONS)) {
//                    // 알림 권한 결과 처리 후 위치 권한 요청
//                    checkLocationPermission()
//                } else if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    // 위치 권한 결과 처리
//                    handleLocationPermissionResult(grantResults)
//                }
//            }
//        }
//    }
//    private fun handleLocationPermissionResult(grantResults: IntArray) {
//        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            initMapFragment()
//        } else {
//            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                explainPermissionRequirement()
//            }
//        }
//    }
//
//    private fun explainPermissionRequirement() {
//        AlertDialog.Builder(this)
//            .setTitle("위치 권한 필요")
//            .setMessage("이 앱은 맵 기능을 사용하기 위해 위치 권한이 필요합니다. 위치 권한을 허용해 주세요.")
//            .setPositiveButton("권한 요청") { _, _ ->
//                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//            }
//            .setNegativeButton("취소", null)
//            .show()
//    }
//
//    private fun replaceFragment(fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.frame_layout, fragment)
//            .commit()
//    }
//}
//