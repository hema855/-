package com.example

import com.example.data.repository.TashteebRepository
import com.example.util.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `exact piaster math avoids floating point drift`() {
        val a = MoneyFormatter.parseEgpToPiasters("0.1")!!
        val b = MoneyFormatter.parseEgpToPiasters("0.2")!!
        assertEquals(30L, a + b)
        assertEquals("0.30 ج.م", MoneyFormatter.formatEgp(a + b))
    }

    @Test
    fun `arabic indic digits normalize and format with thousands commas`() {
        val piasters = MoneyFormatter.parseEgpToPiasters("١٣٨٥٠٠")!!
        assertEquals(138_500_00L, piasters)
        assertEquals("138,500 ج.م", MoneyFormatter.formatEgp(piasters))
    }

    @Test
    fun `quantity times unit price calculates accurately`() {
        val total = MoneyFormatter.computeLineTotalPiasters("10", "455.75")!!
        assertEquals(4_557_50L, total)
        assertEquals("4,557.50 ج.م", MoneyFormatter.formatEgp(total))
        assertNull(MoneyFormatter.computeLineTotalPiasters("-5", "100"))
    }

    @Test
    fun `preseeded 8 finishing categories match exact budgets`() {
        val categories = TashteebRepository.defaultCategories()
        assertEquals(8, categories.size)
        val totalBudgetPiasters = categories.sumOf { it.budgetPiasters }
        assertEquals(583_400_00L, totalBudgetPiasters)
    }
}
