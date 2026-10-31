package com.coderpakistan.workmanager.domain.repository

import com.coderpakistan.workmanager.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotes()
    fun getAllQuotes(): Flow<List<Quote>>
    fun setPeriodicWorkRequest()
}