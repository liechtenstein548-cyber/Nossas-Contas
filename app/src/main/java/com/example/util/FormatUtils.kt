package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
        currencySymbol = "R$ "
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val decimalFormat = DecimalFormat("R$ #,##0.00", symbols)

    fun format(value: Double): String {
        return decimalFormat.format(value)
    }

    /**
     * Parses user input into a Double. Supports formats like "1250,50", "1250.50", "1.250,50", etc.
     */
    fun parseAmount(input: String): Double {
        if (input.isBlank()) return 0.0
        val clean = input.replace("R$", "")
            .replace(" ", "")
            .trim()
        
        val normalized = if (clean.contains(".") && clean.contains(",")) {
            clean.replace(".", "").replace(",", ".")
        } else if (clean.contains(",")) {
            clean.replace(",", ".")
        } else {
            clean
        }
        return normalized.toDoubleOrNull() ?: 0.0
    }
}

object DateUtils {
    private val ptBrLocale = Locale("pt", "BR")
    val isoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayDayMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val displayFullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", ptBrLocale)

    fun formatMonthYearHeader(yearMonth: YearMonth): String {
        val monthName = yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, ptBrLocale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
        return "$monthName ${yearMonth.year}"
    }

    fun toYearMonthString(yearMonth: YearMonth): String {
        return "%04d-%02d".format(yearMonth.year, yearMonth.monthValue)
    }

    fun parseYearMonth(yearMonthStr: String): YearMonth {
        return try {
            val parts = yearMonthStr.split("-")
            YearMonth.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            YearMonth.now()
        }
    }

    fun formatDueDateShort(isoDate: String): String {
        if (isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return "Sem vencimento"
        return try {
            val date = LocalDate.parse(isoDate, isoFormatter)
            date.format(displayDayMonthFormatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getDaysUntilDue(isoDate: String): Long {
        if (isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return 999L
        return try {
            val date = LocalDate.parse(isoDate, isoFormatter)
            ChronoUnit.DAYS.between(LocalDate.now(), date)
        } catch (e: Exception) {
            999L
        }
    }

    fun isOverdue(isoDate: String, isPaid: Boolean): Boolean {
        if (isPaid || isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return false
        return getDaysUntilDue(isoDate) < 0
    }

    /**
     * Checks if a bill is due within 10 days (from today up to 10 days in the future).
     */
    fun isDueWithin10Days(isoDate: String, isPaid: Boolean): Boolean {
        if (isPaid || isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return false
        val days = getDaysUntilDue(isoDate)
        return days in 0..10
    }

    fun formatDueStatus(isoDate: String, isPaid: Boolean): String {
        if (isPaid) return "Paga"
        if (isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return "Sem vencimento"
        val days = getDaysUntilDue(isoDate)
        return when {
            days < 0 -> "Atrasada há ${-days} ${if (days == -1L) "dia" else "dias"}"
            days == 0L -> "Vence hoje"
            days == 1L -> "Vence amanhã"
            days <= 10L -> "Vence em $days dias"
            else -> "Venc. ${formatDueDateShort(isoDate)}"
        }
    }

    /**
     * Calculates the due date for a given number of months in the future,
     * maintaining the day of the month (or clamping to the last valid day).
     */
    fun calculateNextMonthDueDate(isoDate: String, monthsToAdd: Long): String {
        if (isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return ""
        return try {
            val date = LocalDate.parse(isoDate, isoFormatter)
            val targetMonth = YearMonth.of(date.year, date.month).plusMonths(monthsToAdd)
            val targetDay = date.dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth())
            targetMonth.atDay(targetDay).format(isoFormatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Extracts yyyy-MM from an ISO date string.
     */
    fun getMonthYearFromIso(isoDate: String): String {
        if (isoDate.isBlank() || isoDate == "SEM_VENCIMENTO") return toYearMonthString(YearMonth.now())
        return try {
            val date = LocalDate.parse(isoDate, isoFormatter)
            toYearMonthString(YearMonth.of(date.year, date.month))
        } catch (e: Exception) {
            toYearMonthString(YearMonth.now())
        }
    }
}
