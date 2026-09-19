package com.example.digitaldetox.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

class UsageAggregationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AggregationWorker", "Aggregating daily usage...")
        // TODO: Access UsageStatsHelper and UsageDao to aggregate data
        return Result.success()
    }
}
