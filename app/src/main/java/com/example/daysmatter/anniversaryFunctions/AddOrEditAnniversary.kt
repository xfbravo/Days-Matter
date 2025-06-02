package com.example.daysmatter.anniversaryFunctions

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.daysmatter.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

//添加纪念日界面
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventName(
    name:String,
    onNameChange: (String) -> Unit
){
    val keyboardController= LocalSoftwareKeyboardController.current
    Card(
        modifier= Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, color = colorResource(id = R.color.black)),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        OutlinedTextField(
            value= name,
            onValueChange =onNameChange,
            modifier= Modifier.fillMaxWidth(),
            placeholder = { Text("请输入纪念日名称") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions=KeyboardActions(onDone={keyboardController?.hide()})
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseDate(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    Card(
        modifier= Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, color = colorResource(id = R.color.black)),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "添加提醒",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(20.dp) // 设置宽度为60dp
            )
            Text("目标日: $selectedDate", color =colorResource(id= R.color.blue) )
            Spacer(modifier = Modifier.weight(1f)) // 占据剩余空间
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.Settings, contentDescription = "选择日期")
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {

                            datePickerState.selectedDateMillis?.let { millis ->
                                onDateSelected(
                                    Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                )
                            }
                            showDatePicker = false
                        }
                    ) { Text("确定") }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text("选择日期", modifier = Modifier.padding(16.dp)) }
                )
            }
        }
    }
}


@Composable
fun RepeatEventType(
    selectedRepeatType: Int = 0, // 默认选择不重复
    onRepeatTypeSelected: (Int) -> Unit
) {
    val repeatTypes = mapOf(
        0 to "不重复",
        1 to "每年",
        2 to "每月",
        3 to "每周",
    )
    Card(
        modifier= Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, color = colorResource(id = R.color.black)),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeatTypes.forEach {
                val isSelected= it.key == selectedRepeatType
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = isSelected,
                        onClick = { onRepeatTypeSelected(it.key) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(20.dp) // 设置宽度为60dp
                    )
                    Text(text = it.value)
                }
            }
        }
    }
}

@Composable
fun Reminder(
    reminderInterval:String?,
    daysBefore:Int?,
    onReminderClick:()->Unit
){
    // 显示提醒设置的文本
    val displayText = when {
        reminderInterval.isNullOrEmpty() -> "未设置"
        daysBefore != null -> "${reminderInterval}提醒，事件前${daysBefore}天提醒"
        else -> "${reminderInterval}提醒"
    }
    Card(
        modifier= Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, color = colorResource(id = R.color.black)),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "添加提醒",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("定期提醒")
            Spacer(modifier = Modifier.weight(1f)) // 占据剩余空间
            Text(
                text = displayText,// 显示提醒设置的文本
                color = colorResource(id = R.color.blue)
            )
            IconButton(onClick = onReminderClick) {
                Icon(Icons.Default.Settings, contentDescription = "选择日期")
            }
        }
    }
}