package com.example

import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class ExampleUnitTest {
  @Test
  fun testCurrencyFormatting() {
    val formatted = CurrencyFormatter.format(1250.00)
    assertTrue(formatted.contains("1.250,00"))
  }

  @Test
  fun testCurrencyParsing() {
    assertEquals(1250.50, CurrencyFormatter.parseAmount("1250,50"), 0.001)
    assertEquals(1250.50, CurrencyFormatter.parseAmount("1.250,50"), 0.001)
    assertEquals(450.00, CurrencyFormatter.parseAmount("450"), 0.001)
  }

  @Test
  fun testMonthYearFormatting() {
    val ym = YearMonth.of(2026, 10)
    val header = DateUtils.formatMonthYearHeader(ym)
    assertTrue(header.contains("2026"))
    assertEquals("2026-10", DateUtils.toYearMonthString(ym))
  }

  @Test
  fun test13OccurrencesMonthlySequencing() {
    val baseYm = YearMonth.of(2026, 10)
    val totalOccurrences = 13
    val monthsList = (0 until totalOccurrences).map { offset ->
      DateUtils.toYearMonthString(baseYm.plusMonths(offset.toLong()))
    }
    // 13 occurrences
    assertEquals(13, monthsList.size)
    // Starting in October 2026
    assertEquals("2026-10", monthsList.first())
    // 13th occurrence is recorded in October 2027
    assertEquals("2027-10", monthsList.last())
    // All occurrences are in distinct months (no duplicates)
    assertEquals(13, monthsList.distinct().size)
  }

  @Test
  fun testGreetingExclamationPoint() {
    fun formatGreeting(name: String?): String {
      return if (!name.isNullOrBlank()) "Olá, ${name.trim()}!" else "Olá!"
    }
    assertEquals("Olá, Carlos!", formatGreeting("Carlos"))
    assertEquals("Olá, Maria Silva!", formatGreeting("Maria Silva"))
    assertEquals("Olá!", formatGreeting(null))
    assertEquals("Olá!", formatGreeting(""))
    assertEquals("Olá!", formatGreeting("   "))
  }
}
