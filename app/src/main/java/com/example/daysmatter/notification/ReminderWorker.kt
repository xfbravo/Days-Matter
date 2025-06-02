package com.example.daysmatter.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val name = inputData.getString("name") ?: return Result.failure()
        val targetDateStr = inputData.getString("targetDate") ?: return Result.failure()
        val daysBeforeReminder = inputData.getInt("daysBeforeReminder", -1)
        val reminderType = inputData.getString("reminderType") ?: "ON_DAY"

        val targetDate = LocalDate.parse(targetDateStr)
        val today = LocalDate.now()

        val title = "纪念日提醒"
        val message = when (reminderType) {
            "ON_DAY" -> "今天是 $name"
            "DAYS_BEFORE" -> "距离 $name 还有 $daysBeforeReminder 天"
            "INTERVAL" -> "距离 $name 还有 ${ChronoUnit.DAYS.between(today, targetDate)} 天"
            else -> return Result.failure()
        }
        Log.d("ReminderWorker", "Showing notification: $title - $message")

        NotificationUtil.showNotification(applicationContext, title, message, id = targetDate.hashCode())
        return Result.success()
    }
}