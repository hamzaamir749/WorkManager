package com.coderpakistan.workmanager.data.mappers

import com.coderpakistan.workmanager.data.model.QuoteDTO
import com.coderpakistan.workmanager.domain.model.Quote

fun QuoteDTO.toDomain(workType: String): Quote {
    return Quote(author, id, quote, workType)
}