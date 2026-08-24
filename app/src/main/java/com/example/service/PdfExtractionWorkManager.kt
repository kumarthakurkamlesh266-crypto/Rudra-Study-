package com.example.service

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/**
 * Helper class to schedule, trigger, observe, and cancel WorkManager PDF Question Extraction tasks.
 */
object PdfExtractionWorkManager {

    const val UNIQUE_WORK_NAME = "pdf_question_extraction_unique_work"
    const val PERIODIC_WORK_NAME = "pdf_question_extraction_periodic_work"
    const val TAG_PDF_EXTRACTION = "tag_pdf_question_extraction"

    /**
     * Enqueues a one-time WorkManager task to extract questions from stored PDFs
     * and insert them into the Question Database.
     */
    fun enqueueExtractionWork(
        context: Context,
        targetDocId: Long = -1L,
        forceReanalyze: Boolean = false
    ): Operation {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putLong(PdfQuestionExtractionWorker.KEY_DOC_ID, targetDocId)
            .putBoolean(PdfQuestionExtractionWorker.KEY_FORCE_REANALYZE, forceReanalyze)
            .build()

        val extractionRequest = OneTimeWorkRequestBuilder<PdfQuestionExtractionWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(TAG_PDF_EXTRACTION)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        return WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            extractionRequest
        )
    }

    /**
     * Schedules periodic background extraction to automatically check and parse
     * any newly uploaded or unextracted stored PDFs.
     */
    fun schedulePeriodicExtraction(
        context: Context,
        intervalHours: Long = 24L
    ): Operation {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<PdfQuestionExtractionWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(TAG_PDF_EXTRACTION)
            .build()

        return WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Cancels active or enqueued PDF Question Extraction work.
     */
    fun cancelExtractionWork(context: Context): Operation {
        return WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /**
     * Returns a Flow of WorkInfo for the unique background extraction task.
     */
    fun getWorkInfoFlow(context: Context): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
            .asFlow()
    }
}
