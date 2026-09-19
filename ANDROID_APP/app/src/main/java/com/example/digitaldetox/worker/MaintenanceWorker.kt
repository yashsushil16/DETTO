package com.example.digitaldetox.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

class MaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MaintenanceWorker", "Cleaning up old data...")
        // TODO: Delete usage logs older than 30/90 days depending on settings
        return Result.success()
    }
}
