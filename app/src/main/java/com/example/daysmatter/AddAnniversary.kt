package com.example.daysmatter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.daysmatter.anniversaryFunctions.ChooseDate
import com.example.daysmatter.anniversaryFunctions.EventName
import com.example.daysmatter.anniversaryFunctions.RemindInAdvance
import com.example.daysmatter.anniversaryFunctions.Reminder
import com.example.daysmatter.anniversaryFunctions.RepeatEventType
import com.example.daysmatter.anniversaryFunctions.SelectRemindLoop
import com.example.daysmatter.db.DaysMatterApplication
import com.example.daysmatter.entity.AnniversaryEntity
import com.example.daysmatter.notification.scheduleReminder
import com.example.daysmatter.ui.theme.DaysMatterTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import kotlinx.coroutines.launch

class AddAnniversary: ComponentActivity(){
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaysMatterTheme {
                val navController= rememberNavController()
                // 提升的状态
                var eventName by remember { mutableStateOf("") }
                var selectedDate by remember { mutableStateOf(LocalDate.now()) }
                var selectedRepeatType by remember { mutableIntStateOf(0) } // 0=不重复,1=每年...
                var reminderInterval by remember { mutableStateOf<String?>(null) }
                var daysBeforeReminder by remember { mutableStateOf<Int?>(null) }

                val onReminderChange: (String?, Int?) -> Unit = { interval, daysBefore ->
                    reminderInterval = interval
                    daysBeforeReminder = daysBefore
                }
                NavHost(
                    navController= navController,
                    startDestination = "add_anniversary"
                ){
                    composable("add_anniversary") {
                        AddAnniversaryScreen(
                            eventName = eventName,
                            onEventNameChange = { eventName = it },
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            selectedRepeatType = selectedRepeatType,
                            onRepeatTypeSelected = { selectedRepeatType = it },
                            reminderInterval = reminderInterval,
                            daysBeforeReminder = daysBeforeReminder,
                            onBack = { finish() },
                            // 保存纪念日
                            onSave = { anniversary ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DaysMatterApplication.database.anniversaryDao()
                                        .insert(anniversary)
                                    scheduleReminder(this@AddAnniversary,anniversary)
                                }
                                finish()
                            },
                            onNavigateToReminder = {// 跳转到设置提醒界面
                                navController.navigate("reminder")
                            }
                        )
                    }

                    composable("reminder") {
                        ReminderScreen(
                            initialInterval = reminderInterval,
                            initialDaysBefore = daysBeforeReminder,
                            onBack = { navController.popBackStack() },
                            onConfirm = { interval, daysBefore ->
                                onReminderChange(interval, daysBefore)
                                navController.popBackStack()// 返回到添加纪念日界面
                            }
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnniversaryScreen(
    eventName: String,
    onEventNameChange: (String) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    selectedRepeatType: Int,
    onRepeatTypeSelected: (Int) -> Unit,
    reminderInterval: String?,
    daysBeforeReminder: Int?,
    onBack: () -> Unit,
    onSave: (AnniversaryEntity) -> Unit,
    onNavigateToReminder: () -> Unit
){
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("添加新的纪念日")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (eventName.isBlank()) {
                                // 显示错误提示
                                return@TextButton
                            }
                            onSave(
                                // 创建新的纪念日实体
                                AnniversaryEntity(
                                    name = eventName,
                                    targetDate = selectedDate,
                                    repeatType = selectedRepeatType,
                                    reminderInterval = reminderInterval,
                                    daysBeforeReminder = daysBeforeReminder
                                )
                            )
                        }
                    ) {
                        Text(text="保存", fontSize =20.sp, color = colorResource(R.color.black))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.blue) // 设置topBar背景颜色
                )
            )
        }
    ){innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            //输入纪念日名称
            EventName(
                name=eventName,
                onNameChange = onEventNameChange
            )
            Spacer(modifier = Modifier.height(5.dp)) // 添加间隔

            //目标日
            ChooseDate(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            Spacer(modifier = Modifier.height(5.dp)) // 添加间隔

            //选择重复类型
            RepeatEventType(
                selectedRepeatType = selectedRepeatType,
                onRepeatTypeSelected = onRepeatTypeSelected
            )
            Spacer(modifier = Modifier.height(10.dp)) // 添加间隔

            //设置提醒
            Reminder(
                reminderInterval = reminderInterval,
                daysBefore = daysBeforeReminder,
                onReminderClick = onNavigateToReminder
            )
        }

    }
}

// 设置提醒界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    initialInterval:String?,
    initialDaysBefore:Int?,

    onBack:()->Unit,
    onConfirm:(String?,Int?)->Unit
) {
    var selectedInterval by remember { mutableStateOf(initialInterval) }
    var daysBefore by remember { mutableStateOf(initialDaysBefore?.toString() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("设置提醒")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onConfirm(
                                selectedInterval?.takeIf{it.isNotEmpty()},
                                daysBefore.toIntOrNull()
                            )
                        }
                    ) {
                        Text("确定",fontSize = 20.sp, color = colorResource(R.color.black))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.blue) // 设置topBar背景颜色
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // 选择多久提醒一次（每天、每周、每月、每年、不提醒）
            SelectRemindLoop(
                selectedInterval= selectedInterval,
                onIntervalSelected = {
                    selectedInterval = it
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            //距离目标日提前多少天提醒一次
            RemindInAdvance(
                daysBefore= daysBefore,
                onDaysBeforeChanged = {
                    daysBefore = it
                }
            )
        }
    }
}

