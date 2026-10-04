package com.example.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val DISPLAY_FORMAT = SimpleDateFormat("dd MMMM yyyy", Locale.US)
    private val SHORT_DISPLAY_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val MONTH_YEAR_FORMAT = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getTodayDate(): String {
        return DATE_FORMAT.format(Date())
    }

    fun formatDate(dateStr: String): String {
        return try {
            val d = DATE_FORMAT.parse(dateStr) ?: return dateStr
            DISPLAY_FORMAT.format(d)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatShortDate(dateStr: String): String {
        return try {
            val d = DATE_FORMAT.parse(dateStr) ?: return dateStr
            SHORT_DISPLAY_FORMAT.format(d)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatMonthYear(cal: Calendar): String {
        return MONTH_YEAR_FORMAT.format(cal.time)
    }

    fun addDays(dateStr: String, days: Int): String {
        return try {
            val d = DATE_FORMAT.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.DAY_OF_YEAR, days)
            DATE_FORMAT.format(cal.time)
        } catch (_: Exception) {
            getTodayDate()
        }
    }

    fun isToday(dateStr: String): Boolean {
        return dateStr == getTodayDate()
    }

    fun daysBetween(fromDateStr: String, toDateStr: String): Int {
        return try {
            val from = DATE_FORMAT.parse(fromDateStr) ?: return 0
            val to = DATE_FORMAT.parse(toDateStr) ?: return 0
            val diff = to.time - from.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) {
            0
        }
    }
}
