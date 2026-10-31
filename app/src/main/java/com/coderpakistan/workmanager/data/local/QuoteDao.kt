package com.coderpakistan.workmanager.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.coderpakistan.workmanager.domain.model.Quote
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: Quote)

    @Query("SELECT * FROM QUOTE ORDER BY time DESC")
    fun getAllQuotes(): Flow<List<Quote>>
}