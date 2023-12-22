package com.adam.tastylog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.adam.tastylog.databinding.FragmentHomeBinding
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback


class HomeFragment : Fragment() ,OnMapReadyCallback{
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()




        return binding.root
    }

    override fun onMapReady(p0: NaverMap) {
        TODO("Not yet implemented")
    }


}