package com.coderpakistan.workmanager.data.remote

import com.coderpakistan.workmanager.data.model.QuoteDTO
import retrofit2.http.GET

interface ApiService {
    @GET("quotes/random")
    suspend fun getQuotes(): QuoteDTO
}