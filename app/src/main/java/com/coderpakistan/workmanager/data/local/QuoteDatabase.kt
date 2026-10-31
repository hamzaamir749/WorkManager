package com.coderpakistan.workmanager.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.coderpakistan.workmanager.domain.model.Quote

@Database(entities = [Quote::class], version = 1, exportSchema = false)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun getQuoteDao(): QuoteDao

    companion object {
        fun getInstance(context: Context) =
            Room.databaseBuilder(context, QuoteDatabase::class.java, "quote_db")
                .build()

    }
}
