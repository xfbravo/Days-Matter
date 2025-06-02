package com.example.daysmatter.notification

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.daysmatter.adjustDateForRepeat
import com.example.daysmatter.entity.AnniversaryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.text.format

fun scheduleReminder(context: Context, entity: AnniversaryEntity) {
    val today = LocalDate.now()
    val finalTargetDate =  adjustDateForRepeat(entity.targetDate, entity.repeatType)
    if (finalTargetDate.isBefore(today) && entity.repeatType == 0) {
        return // 不重复且已过期的纪念日不设置提醒
    }

    if (entity.repeatType == 0 && finalTargetDate.isBefore(today)) return // 不重复且已过期

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val baseId="${entity.name}_${entity.targetDate}"

    val workManager = WorkManager.getInstance(context)

    if (!finalTargetDate.isBefore(today)) {
        val delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), finalTargetDate.atStartOfDay())
        Log.d("Reminder", "On-day reminder delay: $delayMillis ms")

        if (delayMillis > 0) {
            val onDayRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, MILLISECONDS)
                .setInputData(workDataOf(
                    "name" to entity.name,
                    "targetDate" to finalTargetDate.format(formatter),
                    "reminderType" to "ON_DAY"
                ))
                .build()

            workManager.enqueueUniqueWork(
                "reminder_on_$baseId",
                ExistingWorkPolicy.REPLACE,
                onDayRequest
            )
        }
    }


    // 2. 提前提醒
    if (entity.daysBeforeReminder != null&& entity.daysBeforeReminder > 0) {
        val beforeDate = finalTargetDate.minusDays(entity.daysBeforeReminder.toLong())
        if (!beforeDate.isBefore(today)) {
            val beforeDelayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), beforeDate.atStartOfDay())
            Log.d("Reminder", "Before reminder delay: $beforeDelayMillis ms")
            if( beforeDelayMillis > 0) {
                val beforeRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(beforeDelayMillis, MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "name" to entity.name,
                            "targetDate" to finalTargetDate.format(formatter),
                            "daysBeforeReminder" to entity.daysBeforeReminder,
                            "reminderType" to "DAYS_BEFORE"
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "reminder_before_${baseId}",
                    ExistingWorkPolicy.REPLACE,
                    beforeRequest
                )
            }
        }
    }

    // 3. 周期提醒
    if (!entity.reminderInterval.isNullOrEmpty() && entity.reminderInterval != "不提醒") {
        val intervalDays = mapIntervalToDays(entity.reminderInterval!!)
        if (intervalDays > 0) {
            // 对于周期提醒，我们只需要设置一次，它会自动重复
            val initialDelay = if (finalTargetDate.isAfter(today)) {
                ChronoUnit.MILLIS.between(LocalDateTime.now(), finalTargetDate.atStartOfDay())
            } else {
                // 对于已经过去的重复事件，计算下一个触发日期
                val nextDate = adjustDateForRepeat(finalTargetDate, entity.repeatType)
                ChronoUnit.MILLIS.between(LocalDateTime.now(), nextDate.atStartOfDay())
            }

            Log.d("Reminder", "Periodic reminder initial delay: $initialDelay ms")

            if (initialDelay > 0) {
                val intervalRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                    intervalDays * 24 * 60 * 60 * 1000L, // 转换为毫秒
                    MILLISECONDS
                )
                    .setInitialDelay(initialDelay, MILLISECONDS)
                    .setInputData(workDataOf(
                        "name" to entity.name,
                        "targetDate" to finalTargetDate.format(formatter),
                        "reminderType" to "INTERVAL"
                    ))
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "reminder_repeat_$baseId",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    intervalRequest
                )
            }
        }
    }
}
fun cancelReminder(context: Context, entity: AnniversaryEntity) {
    val baseId = "${entity.name}_${entity.targetDate}"
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork("reminder_on_${baseId}")
    workManager.cancelUniqueWork("reminder_before_${baseId}")
    workManager.cancelUniqueWork("reminder_repeat_${baseId}")
}
fun mapIntervalToDays(interval: String): Long {
    return when (interval.trim()) {
        "每天" -> 1
        "每周" -> ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusWeeks(1))
        "每月" -> ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(1))
        "每年" -> ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusYears(1))
        "不提醒" -> 0
        else -> 0
    }
}