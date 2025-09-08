package com.hater.githubsearch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GithubSearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
): ViewModel() {

    private val keyword = MutableStateFlow("")

    private val _searchPagingResult = MutableStateFlow<PagingData<UserInfo>>(PagingData.empty())
    val searchPagingResult: StateFlow<PagingData<UserInfo>> = _searchPagingResult.asStateFlow()

    val debouncedKeyword: StateFlow<String> = keyword
        .debounce(2000L)
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun updateKeyword(text: String) {
        keyword.value = text
    }

    fun searchUser(query: String) {
        viewModelScope.launch {
            searchRepository.searchUserPaging(query)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _searchPagingResult.value = it
                }
        }
    }
}