package com.example.daysmatter

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daysmatter.ui.theme.DaysMatterTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.res.colorResource
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.daysmatter.db.DaysMatterApplication
import com.example.daysmatter.entity.AnniversaryEntity
import com.example.daysmatter.notification.ReminderWorker
import java.util.concurrent.TimeUnit
import kotlin.toString

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            DaysMatterTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                                ) {
                                    Text("Days Matter")
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    val intent=Intent(this@MainActivity, AddAnniversary::class.java)
                                    startActivity(intent) // 点击添加按钮跳转到添加纪念日界面
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "添加")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colorResource(R.color.blue) // 设置topBar背景颜色
                            )
                        )
                    }
                ) { innerPadding ->
                    DaysMatter(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

    }

}


@Composable
fun DaysMatter(modifier: Modifier){
    val events = DaysMatterApplication.database.anniversaryDao()
        .getAllAnniversaries()
        .collectAsState(initial= emptyList()).value
    val context=LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        EventList(
            events = events,
            onItemClick = { anniversary ->
                val intent = Intent(context, AnniversaryDetail::class.java).apply {
                    putExtra("anniversary", anniversary) // 传递纪念日ID
                }
                context.startActivity(intent) // 点击事件跳转到纪念日详情界面
            }
        )

    }
}

@Composable
fun EventList(events: List<AnniversaryEntity>, onItemClick: (AnniversaryEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

    ) {
        items(events.size) { index ->
            EventItem(events[index], onItemClick)
            Spacer(modifier = Modifier.height(8.dp)) // 添加间隔
        }
    }

}

@Composable
fun EventItem(event: AnniversaryEntity, onItemClick: (AnniversaryEntity) -> Unit) {
    val today = LocalDate.now()
    val adjustedDate=adjustDateForRepeat(event.targetDate, event.repeatType)
    val isPast=today.isAfter(adjustedDate)
    val days=if (isPast) -daysUntil(adjustedDate) else daysUntil(adjustedDate)
    val numberColor= if(isPast) colorResource(id = R.color.lightOrange) else colorResource(id = R.color.lightBlue)
    val dayColor= if(isPast) colorResource(id = R.color.orange) else colorResource(id = R.color.blue)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(event) },
        shape = RoundedCornerShape(8.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "  ${event.name} ${if(isPast) "已经" else "还有"}",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f) // 让文本占据剩余空间
            )
            Box(
                modifier = Modifier
                    .background(color = numberColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$days ",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .background(color = dayColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .width(25.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "天",
                    fontSize = 18.sp,
                    color = Color.White,
                )
            }
        }
    }
}
fun adjustDateForRepeat(originalDate: LocalDate, repeatType: Int): LocalDate {
    val today = LocalDate.now()
    if (originalDate.isAfter(today)) return originalDate

    return when(repeatType) {
        1 -> { // 每年
            var nextDate = originalDate.withYear(today.year)
            if (nextDate.isBefore(today)) nextDate = nextDate.withYear(today.year + 1)
            nextDate
        }
        2 -> { // 每月
            var nextDate = originalDate.withYear(today.year).withMonth(today.monthValue)
            if (nextDate.isBefore(today)) nextDate = nextDate.plusMonths(1)
            nextDate
        }
        3 -> { // 每周
            var nextDate = originalDate
            while (nextDate.isBefore(today)) {
                nextDate = nextDate.plusWeeks(1)
            }
            nextDate
        }
        else -> originalDate // 不重复
    }
}

fun daysUntil(date: LocalDate): Long {
    val today = LocalDate.now()
    return ChronoUnit.DAYS.between(today, date)
}
