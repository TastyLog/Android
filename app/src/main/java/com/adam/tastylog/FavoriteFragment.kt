package com.adam.tastylog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.adam.tastylog.databinding.FragmentFavoriteBinding



class FavoriteFragment : Fragment() {
    lateinit var binding: FragmentFavoriteBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()




        return binding.root
    }



}