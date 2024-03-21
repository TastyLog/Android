package com.adam.tastylog.ui.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.adam.tastylog.R
import com.adam.tastylog.utils.UpdateSearchTermWorker
import com.adam.tastylog.databinding.ActivityMainBinding
import com.adam.tastylog.ui.fragment.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit


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

//        checkNotificationPermission()

        locationServiceLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

//    private fun checkNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            if (!manager.areNotificationsEnabled()) {
//                // 알림이 비활성화되어 있으면 사용자에게 활성화를 요청하는 대화상자 표시
//                showNotificationPermissionDialog()
//            }
//        }
//    }

//    private fun showNotificationPermissionDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("알림 권한 필요")
//            .setMessage("이 앱은 알림 기능을 사용하기 위해 알림 권한이 필요합니다. 알림 설정을 활성화해 주세요.")
//            .setPositiveButton("설정으로 이동") { _, _ ->
//                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//                }
//                startActivity(intent)
//            }
//            .setNegativeButton("취소", null)
//            .show()
//    }

}
