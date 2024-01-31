package com.adam.tastylog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adam.tastylog.repository.YoutuberRepository

class YoutuberViewModelFactory(private val repository: YoutuberRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YoutuberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return YoutuberViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}