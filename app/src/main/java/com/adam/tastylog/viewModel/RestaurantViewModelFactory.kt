package com.adam.tastylog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adam.tastylog.repository.RestaurantRepository

class RestaurantViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
