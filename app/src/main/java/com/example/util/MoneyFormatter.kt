package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {
    private val usSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val wholeFormatter = DecimalFormat("#,##0", usSymbols)
    private val decimalFormatter = DecimalFormat("#,##0.00", usSymbols)
    private val quantityFormatter = DecimalFormat("#,##0.##", usSymbols)

    private val arabicDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

    /**
     * Normalizes Arabic-Indic digits (٠-٩) and comma/dot separators to standard ASCII digits
     * so users typing with an Arabic keyboard experience zero friction.
     */
    fun normalizeNumericInput(raw: String): String {
        val sb = StringBuilder(raw.length)
        for (ch in raw) {
            when (ch) {
                '٠', '۰' -> sb.append('0')
                '١', '۱' -> sb.append('1')
                '٢', '۲' -> sb.append('2')
                '٣', '۳' -> sb.append('3')
                '٤', '۴' -> sb.append('4')
                '٥', '۵' -> sb.append('5')
                '٦', '۶' -> sb.append('6')
                '٧', '۷' -> sb.append('7')
                '٨', '۸' -> sb.append('8')
                '٩', '۹' -> sb.append('9')
                '٫', '،' -> sb.append('.')
                ',' -> Unit // ignore thousands grouping commas
                else -> if (ch.isDigit() || ch == '.') sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Exact EGP string -> integer piasters (1 EGP = 100 piasters). Zero floating-point error.
     */
    fun parseEgpToPiasters(raw: String): Long? {
        val cleaned = normalizeNumericInput(raw).trim()
        if (cleaned.isEmpty() || cleaned == ".") return null
        return try {
            val bd = BigDecimal(cleaned)
            if (bd < BigDecimal.ZERO) return null
            bd.multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    fun parsePositiveDouble(raw: String): Double? {
        val cleaned = normalizeNumericInput(raw).trim()
        if (cleaned.isEmpty() || cleaned == ".") return null
        return try {
            val v = cleaned.toDouble()
            if (v > 0.0 && v.isFinite()) v else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Computes exact quantity * unitPrice in piasters using BigDecimal arithmetic.
     */
    fun computeLineTotalPiasters(quantityRaw: String, unitPriceRaw: String): Long? {
        val qClean = normalizeNumericInput(quantityRaw).trim()
        val pClean = normalizeNumericInput(unitPriceRaw).trim()
        if (qClean.isEmpty() || pClean.isEmpty() || qClean == "." || pClean == ".") return null
        return try {
            val qty = BigDecimal(qClean)
            val price = BigDecimal(pClean)
            if (qty <= BigDecimal.ZERO || price < BigDecimal.ZERO) return null
            qty.multiply(price)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats integer piasters into EGP with automatic thousands commas (e.g., "138,500 ج.م" or "125.50 ج.م").
     */
    fun formatEgp(piasters: Long, includeCurrency: Boolean = true): String {
        val isNegative = piasters < 0
        val absPiasters = abs(piasters)
        val major = absPiasters / 100
        val minor = absPiasters % 100
        val numberText = if (minor == 0L) {
            wholeFormatter.format(major)
        } else {
            decimalFormatter.format(BigDecimal(absPiasters).divide(BigDecimal(100)))
        }
        val signed = if (isNegative) "-$numberText" else numberText
        return if (includeCurrency) "$signed ج.م" else signed
    }

    /**
     * Plain numeric string suitable for editing in a TextField (no grouping commas).
     */
    fun piastersToEditableString(piasters: Long): String {
        if (piasters == 0L) return ""
        val major = piasters / 100
        val minor = abs(piasters % 100)
        return if (minor == 0L) {
            major.toString()
        } else {
            String.format(Locale.US, "%d.%02d", major, minor)
        }
    }

    /**
     * Plain decimal string for CSV export (exact, without grouping commas).
     */
    fun piastersToCsvDecimal(piasters: Long): String {
        val bd = BigDecimal(piasters).divide(BigDecimal(100))
        return if (piasters % 100L == 0L) {
            (piasters / 100L).toString()
        } else {
            bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
        }
    }

    fun formatQuantity(qty: Double): String = quantityFormatter.format(qty)

    fun computePercentage(actualPiasters: Long, budgetPiasters: Long): Float {
        if (budgetPiasters <= 0L) {
            return if (actualPiasters > 0L) 100f else 0f
        }
        return (actualPiasters.toDouble() / budgetPiasters.toDouble() * 100.0).toFloat()
    }

    fun formatPercentage(percent: Float): String {
        val rounded = BigDecimal(percent.toDouble()).setScale(1, RoundingMode.HALF_UP)
        val stripped = if (rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            rounded.toBigInteger().toString()
        } else {
            rounded.toPlainString()
        }
        return "$stripped%"
    }

    fun todayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun yesterdayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatDate(millis: Long): String = synchronized(arabicDateFormat) {
        arabicDateFormat.format(Date(millis))
    }

    fun formatIsoDate(millis: Long): String = synchronized(isoDateFormat) {
        isoDateFormat.format(Date(millis))
    }

    fun formatFileTimestamp(millis: Long = System.currentTimeMillis()): String =
        synchronized(fileTimestampFormat) {
            fileTimestampFormat.format(Date(millis))
        }
}
