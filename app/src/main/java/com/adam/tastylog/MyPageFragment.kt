package com.adam.tastylog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.adam.tastylog.databinding.FragmentMypageBinding


class MyPageFragment : Fragment() {
    lateinit var binding: FragmentMypageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()




        return binding.root
    }



}