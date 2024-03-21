package com.adam.tastylog

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.adam.tastylog.databinding.ActivitySearchRestaurantBinding
import com.adam.tastylog.ui.adapter.SearchHistoryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchRestaurantActivity : AppCompatActivity() {

    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private val searchHistoryList = mutableListOf<String>()
    private lateinit var binding: ActivitySearchRestaurantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 키보드 자동으로 보여주기
        binding.editTextSearch.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editTextSearch, InputMethodManager.SHOW_IMPLICIT)


        setupSearchHistoryRecyclerView()
        loadSearchHistory()

        // 검색 EditText에 대한 리스너 설정
        binding.editTextSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = v.text.toString().trim()
                handleSearchQuery(searchQuery) // 여기에서 handleSearchQuery 호출
                true // 이벤트 처리 완료
            } else {
                false // 다른 액션 ID에 대한 처리가 필요한 경우
            }
        }

        // "취소" 버튼 클릭 리스너 설정
        binding.textviewCancel.setOnClickListener {
            finish() // 액티비티 종료
        }
    }
    private fun setupSearchHistoryRecyclerView() {
        searchHistoryAdapter = SearchHistoryAdapter(
            searchHistoryList,
            onClick = { query -> returnSearchResult(query) },
            onDeleteClick = { query -> removeSearchQuery(query) }
        )
        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryRecyclerView.adapter = searchHistoryAdapter
    }

    private fun loadSearchHistory() {
        val sharedPreferences = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val historySet = sharedPreferences.getStringSet("history", setOf()) ?: setOf()
        searchHistoryList.clear()
        // 검색 기록을 불러와서 리스트에 추가하기 전에 뒤집어 줍니다.
        searchHistoryList.addAll(historySet.sortedByDescending { it }) // 최신 검색어가 위로 오도록 정렬
        searchHistoryAdapter.updateItems(searchHistoryList) // 어댑터에 변경사항 반영
    }


    private fun saveSearchQuery(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreferences = getSharedPreferences("search_history", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val existingHistory = sharedPreferences.getStringSet("history", setOf())?.toMutableSet() ?: mutableSetOf()
            existingHistory.add(query) // 새 쿼리 추가
            editor.putStringSet("history", existingHistory)
            editor.apply()

            // UI 업데이트를 메인 스레드로 전환
            withContext(Dispatchers.Main) {
                loadSearchHistory() // UI 업데이트 관련 코드
            }
        }
    }



    private fun handleSearchQuery(query: String) {
        if (query.isNotEmpty()) {
            // 검색 결과를 먼저 반환
            returnSearchResult(query)
            // 검색 기록 저장은 비동기로 처리
            CoroutineScope(Dispatchers.IO).launch {
                saveSearchQuery(query)
            }
        }
    }

    private fun returnSearchResult(query: String) {
        val intent = Intent().apply {
            putExtra("search_query", query)
        }
        setResult(Activity.RESULT_OK, intent)
        finish() // 이 활동을 종료하고 결과를 반환
    }
    private fun removeSearchQuery(query: String) {
        val sharedPreferences = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val existingHistory = sharedPreferences.getStringSet("history", setOf())?.toMutableSet() ?: mutableSetOf()
        existingHistory.remove(query)
        editor.putStringSet("history", existingHistory)
        editor.apply()
        loadSearchHistory() // 삭제 후 리스트 즉시 업데이트
    }
}