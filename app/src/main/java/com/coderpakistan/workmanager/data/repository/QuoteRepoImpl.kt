package com.coderpakistan.workmanager.data.repository

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.coderpakistan.workmanager.data.local.QuoteDao
import com.coderpakistan.workmanager.data.worker.FetchWorker
import com.coderpakistan.workmanager.data.worker.NotificationWorker
import com.coderpakistan.workmanager.data.worker.PeriodicWorker
import com.coderpakistan.workmanager.domain.model.Quote
import com.coderpakistan.workmanager.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class QuoteRepoImpl(
    private val workManager: WorkManager,
    private val quoteDao: QuoteDao
) : QuoteRepository {
    override fun getQuotes() {
        val workRequest = OneTimeWorkRequestBuilder<FetchWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        val notificationWOrkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .build()
        workManager.beginWith(workRequest)
            .then(notificationWOrkRequest)
            .enqueue()
    }

    override fun getAllQuotes(): Flow<List<Quote>> = quoteDao.getAllQuotes()

    override fun setPeriodicWorkRequest() {

        val workRequest =
            PeriodicWorkRequest.Builder(PeriodicWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        workManager.enqueueUniquePeriodicWork(
            "UniqueWorkRequest",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}