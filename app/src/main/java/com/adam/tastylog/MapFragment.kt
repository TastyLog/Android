package com.adam.tastylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.adam.tastylog.databinding.FragmentMapBinding
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.WindowManager
import com.naver.maps.map.LocationTrackingMode



class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentMapBinding
    private lateinit var naverMap: NaverMap
    private var isMapInit = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MapFragment", "onCreateView called")
        binding = FragmentMapBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(mapObject: NaverMap) {
        Log.d("MapFragment", "onMapReady called")
        naverMap = mapObject
        isMapInit = true

        val uiSettings = naverMap.uiSettings
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = true

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            Log.d("MapFragment", "Location tracking mode set to Follow")
        } else {
            Log.d("MapFragment", "Location permission not granted")
        }

        naverMap.addOnLocationChangeListener { location ->
            Log.d("MapFragment", "Location updated: ${location.latitude}, ${location.longitude}")
        }
    }

    // 나머지 메서드들에도 로그 추가
    override fun onStart() {
        super.onStart()
        Log.d("MapFragment", "onStart called")
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MapFragment", "onResume called")



        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MapFragment", "onPause called")
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d("MapFragment", "onStop called")

        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MapFragment", "onDestroy called")
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("MapFragment", "onSaveInstanceState called")
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("MapFragment", "onLowMemory called")
        binding.mapView.onLowMemory()
    }
}
