package com.adam.tastylog.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adam.tastylog.repository.SearchTermRepository

//
//class RealTimeSearchTermViewModelFactory(private val repository: SearchTermRepository, private val application: Application) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(RealTimeSearchTermViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return RealTimeSearchTermViewModel(repository, application) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
