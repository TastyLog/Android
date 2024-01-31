package com.adam.tastylog.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.tastylog.model.YoutuberModel
import com.adam.tastylog.repository.YoutuberRepository

import kotlinx.coroutines.launch

class YoutuberViewModel(private val youtuberRepository: YoutuberRepository) : ViewModel() {

    private val _youtubers = MutableLiveData<List<YoutuberModel>>()
    val youtubers: LiveData<List<YoutuberModel>> = _youtubers

    fun getYoutubers(){
        viewModelScope.launch {
            try {
                val youtuberList = youtuberRepository.getYoutuberList()
                _youtubers.postValue(youtuberList)
            } catch (e: Exception) {
                Log.e("YoutuberViewModel", "Error fetching youtubers: ${e.message}")
            }
        }
    }
}
