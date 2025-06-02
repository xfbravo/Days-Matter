package com.example.daysmatter.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.daysmatter.db.DaysMatterApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val anniversaries = DaysMatterApplication.database.anniversaryDao().getAllAnniversaries()
            CoroutineScope(Dispatchers.IO).launch {
                anniversaries.collect { list ->
                    list.forEach { anniversary ->
                        scheduleReminder(context, anniversary)
                    }
                }
            }
        }
    }
}