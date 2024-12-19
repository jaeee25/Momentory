package com.example.momentory

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatDateWithYear(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatDateWithoutYear(date: Date, withYear: Boolean = true): String {
        val dateFormat = SimpleDateFormat("MM월 dd일", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatDateWithCalendar(date: Date): String {
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 0부터 시작하므로 +1 필요
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "${year}년 ${month}월 ${day}일"
    }
}
