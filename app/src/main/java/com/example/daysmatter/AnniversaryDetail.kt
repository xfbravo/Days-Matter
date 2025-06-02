package com.example.daysmatter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.daysmatter.anniversaryFunctions.ChooseDate
import com.example.daysmatter.anniversaryFunctions.EventName
import com.example.daysmatter.anniversaryFunctions.Reminder
import com.example.daysmatter.anniversaryFunctions.RepeatEventType
import com.example.daysmatter.db.DaysMatterApplication
import com.example.daysmatter.entity.AnniversaryEntity
import com.example.daysmatter.notification.cancelReminder
import com.example.daysmatter.notification.scheduleReminder
import com.example.daysmatter.ui.theme.DaysMatterTheme
import com.example.daysmatter.viewModel.AnniversaryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants
import java.time.LocalDate


class AnniversaryDetail : ComponentActivity() {
    private val viewModel: AnniversaryViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val anniversary = intent.getParcelableExtra<AnniversaryEntity>("anniversary")
        viewModel.setAnniversary(anniversary!!)
        setContent {
            DaysMatterTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "anniversaryDetail"
                ) {
                    composable("anniversaryDetail") {
                        val anniv by viewModel.anniversaryState.collectAsState()
                        AnniversaryDetailScreen(
                            anniversary = anniv,
                            onBack = { finish() },
                            onEdit = { navController.navigate("editAnniversary") }
                        )
                    }

                    composable("editAnniversary") {
                        val anniv by viewModel.anniversaryState.collectAsState()
                        EditAnniversaryScreen(
                            anniversary = anniv,
                            onBack = { navController.popBackStack() },
                            onSave = {
                                anniv?.let {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        DaysMatterApplication.database.anniversaryDao()
                                            .update(it)
                                        scheduleReminder(this@AnniversaryDetail, it) // 更新后重新调度
                                    }
                                }
                                navController.popBackStack()
                            },
                            onDelete = {
                                anniv?.let {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        DaysMatterApplication.database.anniversaryDao()
                                            .delete(it)
                                        cancelReminder(this@AnniversaryDetail, it)
                                    }
                                }
                                finish()
                            },
                            onNavigateToReminder = {
                                navController.navigate("reminder")
                            },
                            viewModel= viewModel
                        )
                    }

                    composable("reminder") {
                        val anniv by viewModel.anniversaryState.collectAsState()
                        ReminderScreen(
                            initialInterval = anniv?.reminderInterval,
                            initialDaysBefore = anniv?.daysBeforeReminder,
                            onBack = { navController.popBackStack() },
                            onConfirm = { interval, daysBefore ->
                                viewModel.updateReminder(interval, daysBefore)
                                navController.popBackStack()
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
fun AnniversaryDetailScreen(
    anniversary: AnniversaryEntity?,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onEdit) {
                        Text("编辑", fontSize = 20.sp, color = colorResource(R.color.black))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.blue) // 设置topBar背景颜色
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(colorResource(R.color.pink))
                .offset(y=-100.dp), // 向上偏移，避免被顶部栏遮挡
            verticalArrangement = Arrangement.Center,
            horizontalAlignment= Alignment.CenterHorizontally
        ) {
            // 显示纪念日详情内容
            anniversary?.let { anniv ->
                val today = LocalDate.now()
                val adjustedDate=adjustDateForRepeat(anniv.targetDate, anniv.repeatType)
                val isPast=today.isAfter(adjustedDate)
                val days=if (isPast) -daysUntil(adjustedDate) else daysUntil(adjustedDate)
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp),
                    colors= CardDefaults.cardColors(containerColor = colorResource(R.color.blue)),
                    shape = RectangleShape
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = anniv.name+ if(isPast) "已经" else "还有",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(200.dp),
                    colors= CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
                    shape = RectangleShape
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = days.toString(),
                            fontSize = 100.sp,
                            color = Color.Black
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp),
                    colors= CardDefaults.cardColors(containerColor = colorResource(R.color.lightGray)),
                    shape = RectangleShape
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "目标日:$adjustedDate",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnniversaryScreen(
    anniversary: AnniversaryEntity?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToReminder: () -> Unit,
    viewModel: AnniversaryViewModel
) {
    if( anniversary == null) {
        // 如果没有传入纪念日数据，直接返回
        return
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("编辑纪念日")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) { Text("保存",fontSize = 20.sp, color = colorResource(R.color.black)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.blue) // 设置topBar背景颜色
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { showDeleteDialog=true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("删除纪念日")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            EventName(
                name = anniversary.name,
                onNameChange = { viewModel.updateName(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            ChooseDate(
                selectedDate = anniversary.targetDate,
                onDateSelected = { viewModel.updateDate(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            RepeatEventType(
                selectedRepeatType = anniversary.repeatType,
                onRepeatTypeSelected = { viewModel.updateRepeatType(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Reminder(
                reminderInterval = anniversary.reminderInterval,
                daysBefore = anniversary.daysBeforeReminder,
                onReminderClick = onNavigateToReminder
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除这个纪念日吗？此操作无法撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }) {
                        Text("确定", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
