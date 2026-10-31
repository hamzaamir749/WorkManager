package com.coderpakistan.workmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderpakistan.workmanager.domain.model.Quote
import com.coderpakistan.workmanager.domain.use_cases.GetAllQuotesFromDbUseCase
import com.coderpakistan.workmanager.domain.use_cases.GetQuotesUseCase
import com.coderpakistan.workmanager.domain.use_cases.SetupPeriodicWorkRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllQuotesFromDbUseCase: GetAllQuotesFromDbUseCase,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val setupPeriodicWorkRequestUseCase: SetupPeriodicWorkRequestUseCase
) : ViewModel() {

    val uiState = getAllQuotesFromDbUseCase.invoke()
        .map { UiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState(emptyList()))

    init {
        setupPeriodicWorkRequestUseCase.invoke()
    }

    fun getQuote() = getQuotesUseCase.invoke()


}

data class UiState(val data: List<Quote>)