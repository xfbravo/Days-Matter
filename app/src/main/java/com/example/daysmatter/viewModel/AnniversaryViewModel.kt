package com.example.daysmatter.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import com.example.daysmatter.entity.AnniversaryEntity
class AnniversaryViewModel : ViewModel() {
    private val _anniversaryState = MutableStateFlow<AnniversaryEntity?>(null)
    val anniversaryState: StateFlow<AnniversaryEntity?> = _anniversaryState

    fun setAnniversary(anniv: AnniversaryEntity) {
        _anniversaryState.value = anniv
    }

    fun updateName(name: String) {
        _anniversaryState.value = _anniversaryState.value?.copy(name = name)
    }

    fun updateDate(date: LocalDate) {
        _anniversaryState.value = _anniversaryState.value?.copy(targetDate = date)
    }


    fun updateRepeatType(repeatType: Int) {
        _anniversaryState.value = _anniversaryState.value?.copy(repeatType = repeatType)
    }

    fun updateReminder(interval: String?, daysBefore: Int?) {
        _anniversaryState.value = _anniversaryState.value?.copy(
            reminderInterval = interval,
            daysBeforeReminder = daysBefore
        )
    }
}