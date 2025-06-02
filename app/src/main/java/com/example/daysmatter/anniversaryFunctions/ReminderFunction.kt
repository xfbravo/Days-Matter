package com.example.daysmatter.anniversaryFunctions

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.daysmatter.R



@Composable
fun SelectRemindLoop(
    selectedInterval: String?,
    onIntervalSelected: (String?) -> Unit
) {
    val items = listOf("每天", "每周", "每月", "每年", "不提醒")

    Column(modifier = Modifier.padding(16.dp)) {
        Text("提醒频率", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        items.forEach { item ->
            val isSelected = selectedInterval == item ||
                    (selectedInterval == null && item == "不提醒")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onIntervalSelected(if (item == "不提醒") null else item)
                    }
                    .border(1.dp, colorResource(id= R.color.black))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item)
                    if (isSelected) {// 如果当前项被选中，显示勾选图标
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选中",
                            tint = colorResource(id = R.color.blue)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RemindInAdvance(
    daysBefore: String,
    onDaysBeforeChanged: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, colorResource(id= R.color.black))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("提前提醒天数", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = daysBefore,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        onDaysBeforeChanged(it)
                    }
                },
                label = { Text("输入天数") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}