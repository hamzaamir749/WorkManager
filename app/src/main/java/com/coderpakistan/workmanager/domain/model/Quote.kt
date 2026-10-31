package com.coderpakistan.workmanager.domain.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Quote(
    val author: String,
    @PrimaryKey
    val id: Int,
    val quote: String,
    val workType: String = "",
    val time: Long = System.currentTimeMillis()
)
