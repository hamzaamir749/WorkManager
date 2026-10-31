package com.coderpakistan.workmanager.domain.use_cases

import com.coderpakistan.workmanager.domain.repository.QuoteRepository
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(private val quoteRepository: QuoteRepository) {
    operator fun invoke() = quoteRepository.getQuotes()
}