package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.model.CategoryIconKey
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.util.MoneyFormatter
import com.example.util.ReceiptImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val categories: List<CategoryEntity>,
    val contractors: List<ContractorEntity>,
    val expenses: List<ExpenseEntity>,
    val receiptImagesBase64: Map<String, String> = emptyMap()
) {
    val totalBudgetPiasters: Long get() = categories.sumOf { it.budgetPiasters }
    val totalSpentPiasters: Long get() = expenses.sumOf { it.amountPiasters }
}

sealed interface BackupParseResult {
    data class Valid(val payload: BackupPayload) : BackupParseResult
    data class Error(val messageAr: String) : BackupParseResult
}

class TashteebRepository(
    private val database: AppDatabase,
    private val appContext: Context
) {
    private val categoryDao = database.categoryDao()
    private val contractorDao = database.contractorDao()
    private val expenseDao = database.expenseDao()
    private val seedMutex = Mutex()

    val categoriesFlow: Flow<List<CategoryEntity>> = categoryDao.observeAll()
    val contractorsFlow: Flow<List<ContractorEntity>> = contractorDao.observeAll()
    val expensesFlow: Flow<List<ExpenseEntity>> = expenseDao.observeAll()

    /**
     * Pre-seeds ONLY the 8 finishing categories and their estimated budgets (~583,400 EGP).
     * Leaves expenses completely empty and ready for real site data.
     */
    suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (categoryDao.count() == 0) {
                categoryDao.insertAll(defaultCategories())
            }
        }
    }

    suspend fun saveCategory(category: CategoryEntity): Long {
        return if (category.id == 0L) {
            categoryDao.insert(category)
        } else {
            categoryDao.update(category)
            category.id
        }
    }

    /**
     * Returns true if deleted, false if the category still has linked expenses.
     */
    suspend fun deleteCategoryIfEmpty(categoryId: Long): Boolean {
        val count = expenseDao.countByCategory(categoryId)
        if (count > 0) return false
        categoryDao.deleteById(categoryId)
        return true
    }

    suspend fun saveExpense(expense: ExpenseEntity): Long {
        return if (expense.id == 0L) {
            expenseDao.insert(expense)
        } else {
            val previous = expenseDao.getById(expense.id)
            if (previous?.receiptImagePath != null && previous.receiptImagePath != expense.receiptImagePath) {
                ReceiptImageStorage.deleteReceiptFile(appContext, previous.receiptImagePath)
            }
            expenseDao.update(expense)
            expense.id
        }
    }

    suspend fun deleteExpense(expenseId: Long) {
        val existing = expenseDao.getById(expenseId)
        if (existing?.receiptImagePath != null) {
            ReceiptImageStorage.deleteReceiptFile(appContext, existing.receiptImagePath)
        }
        expenseDao.deleteById(expenseId)
    }

    suspend fun saveContractor(contractor: ContractorEntity): Long {
        return if (contractor.id == 0L) {
            contractorDao.insert(contractor)
        } else {
            contractorDao.update(contractor)
            contractor.id
        }
    }

    suspend fun deleteContractor(contractorId: Long) {
        contractorDao.deleteById(contractorId)
    }

    /**
     * Single-source-of-truth contractor payment: writes ONE ExpenseEntity marked as LABOR
     * linked to the contractor so both Contractor balance AND Expenses Ledger stay 100% synced.
     */
    suspend fun recordContractorPayment(
        contractor: ContractorEntity,
        amountPiasters: Long,
        dateMillis: Long,
        paymentMethod: PaymentMethod,
        note: String,
        categoryId: Long = contractor.categoryId
    ): Long {
        val cleanNote = note.trim()
        val description = if (cleanNote.isNotEmpty()) {
            "دفعة ${contractor.name} (${contractor.trade}) — $cleanNote"
        } else {
            "دفعة مصنعية — ${contractor.name} (${contractor.trade})"
        }
        val expense = ExpenseEntity(
            dateMillis = dateMillis,
            categoryId = categoryId,
            expenseType = ExpenseType.LABOR.code,
            description = description,
            quantity = null,
            unitPricePiasters = null,
            amountPiasters = amountPiasters,
            paymentMethod = paymentMethod.code,
            contractorId = contractor.id,
            countsTowardAgreement = true
        )
        return expenseDao.insert(expense)
    }

    suspend fun exportJsonBackup(): String {
        val categories = categoryDao.getAllSnapshot()
        val contractors = contractorDao.getAllSnapshot()
        val expenses = expenseDao.getAllSnapshot()

        val root = JSONObject().apply {
            put("schemaVersion", 2)
            put("appName", "تشطيب 160")
            put("apartmentAreaSqm", 160)
            put("exportedAtMillis", System.currentTimeMillis())

            put("categories", JSONArray().apply {
                categories.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("description", c.description)
                        put("budgetPiasters", c.budgetPiasters)
                        put("iconKey", c.iconKey)
                        put("sortOrder", c.sortOrder)
                        put("isDefault", c.isDefault)
                    })
                }
            })

            put("contractors", JSONArray().apply {
                contractors.forEach { ct ->
                    put(JSONObject().apply {
                        put("id", ct.id)
                        put("name", ct.name)
                        put("trade", ct.trade)
                        put("categoryId", ct.categoryId)
                        put("agreedAmountPiasters", ct.agreedAmountPiasters)
                        put("phone", ct.phone)
                        put("notes", ct.notes)
                        put("createdAtMillis", ct.createdAtMillis)
                    })
                }
            })

            put("expenses", JSONArray().apply {
                expenses.forEach { e ->
                    put(JSONObject().apply {
                        put("id", e.id)
                        put("dateMillis", e.dateMillis)
                        put("categoryId", e.categoryId)
                        put("expenseType", e.expenseType)
                        put("description", e.description)
                        if (e.quantity != null) put("quantity", e.quantity) else put("quantity", JSONObject.NULL)
                        if (e.unitPricePiasters != null) put("unitPricePiasters", e.unitPricePiasters) else put("unitPricePiasters", JSONObject.NULL)
                        put("amountPiasters", e.amountPiasters)
                        put("paymentMethod", e.paymentMethod)
                        if (e.contractorId != null) put("contractorId", e.contractorId) else put("contractorId", JSONObject.NULL)
                        put("countsTowardAgreement", e.countsTowardAgreement)
                        if (e.receiptImagePath != null) {
                            put("receiptImagePath", e.receiptImagePath)
                            val b64 = ReceiptImageStorage.encodeReceiptToBase64(appContext, e.receiptImagePath)
                            if (b64 != null) put("receiptImageBase64", b64)
                        } else {
                            put("receiptImagePath", JSONObject.NULL)
                        }
                        put("createdAtMillis", e.createdAtMillis)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun parseBackupJson(rawJson: String): BackupParseResult {
        return try {
            val root = JSONObject(rawJson)
            val version = root.optInt("schemaVersion", -1)
            if (version != 1 && version != 2) {
                return BackupParseResult.Error("ملف النسخة الاحتياطية غير مدعوم أو إصداره غير متوافق.")
            }

            val categoriesJson = root.optJSONArray("categories")
                ?: return BackupParseResult.Error("ملف النسخة الاحتياطية لا يحتوي على قائمة البنود.")
            val contractorsJson = root.optJSONArray("contractors") ?: JSONArray()
            val expensesJson = root.optJSONArray("expenses") ?: JSONArray()

            val categories = buildList {
                for (i in 0 until categoriesJson.length()) {
                    val obj = categoriesJson.getJSONObject(i)
                    add(
                        CategoryEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            budgetPiasters = obj.getLong("budgetPiasters").coerceAtLeast(0L),
                            iconKey = obj.optString("iconKey", CategoryIconKey.CUSTOM.key),
                            sortOrder = obj.optInt("sortOrder", i),
                            isDefault = obj.optBoolean("isDefault", false)
                        )
                    )
                }
            }

            if (categories.isEmpty()) {
                return BackupParseResult.Error("قائمة البنود في الملف فارغة ولا يمكن الاستعادة.")
            }

            val validCategoryIds = categories.map { it.id }.toSet()
            val fallbackCategoryId = categories.first().id

            val contractors = buildList {
                for (i in 0 until contractorsJson.length()) {
                    val obj = contractorsJson.getJSONObject(i)
                    val catId = obj.optLong("categoryId", fallbackCategoryId)
                    add(
                        ContractorEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            trade = obj.optString("trade", ""),
                            categoryId = if (catId in validCategoryIds) catId else fallbackCategoryId,
                            agreedAmountPiasters = obj.getLong("agreedAmountPiasters").coerceAtLeast(0L),
                            phone = obj.optString("phone", ""),
                            notes = obj.optString("notes", ""),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }

            val validContractorIds = contractors.map { it.id }.toSet()

            val receiptImagesMap = mutableMapOf<String, String>()
            val expenses = buildList {
                for (i in 0 until expensesJson.length()) {
                    val obj = expensesJson.getJSONObject(i)
                    val catId = obj.optLong("categoryId", fallbackCategoryId)
                    val rawContractorId = if (obj.isNull("contractorId")) null else obj.optLong("contractorId")
                    val safeContractorId = rawContractorId?.takeIf { it in validContractorIds }
                    val qty = if (obj.isNull("quantity")) null else obj.optDouble("quantity").takeIf { !it.isNaN() }
                    val unitPrice = if (obj.isNull("unitPricePiasters")) null else obj.optLong("unitPricePiasters")
                    val receiptPath = if (obj.isNull("receiptImagePath")) null else obj.optString("receiptImagePath", "").ifBlank { null }
                    val receiptBase64 = if (obj.isNull("receiptImageBase64")) null else obj.optString("receiptImageBase64", "").ifBlank { null }
                    if (receiptPath != null && receiptBase64 != null) {
                        receiptImagesMap[receiptPath] = receiptBase64
                    }

                    add(
                        ExpenseEntity(
                            id = obj.getLong("id"),
                            dateMillis = obj.getLong("dateMillis"),
                            categoryId = if (catId in validCategoryIds) catId else fallbackCategoryId,
                            expenseType = ExpenseType.fromCode(obj.optString("expenseType", ExpenseType.MATERIALS.code)).code,
                            description = obj.getString("description"),
                            quantity = qty,
                            unitPricePiasters = unitPrice,
                            amountPiasters = obj.getLong("amountPiasters").coerceAtLeast(0L),
                            paymentMethod = PaymentMethod.fromCode(obj.optString("paymentMethod", PaymentMethod.CASH.code)).code,
                            contractorId = safeContractorId,
                            countsTowardAgreement = obj.optBoolean("countsTowardAgreement", true),
                            receiptImagePath = receiptPath,
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }

            BackupParseResult.Valid(
                BackupPayload(
                    schemaVersion = version,
                    exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis()),
                    categories = categories,
                    contractors = contractors,
                    expenses = expenses,
                    receiptImagesBase64 = receiptImagesMap
                )
            )
        } catch (_: Exception) {
            BackupParseResult.Error("تعذر قراءة الملف. تأكد أنه ملف نسخة احتياطية JSON صالح من تطبيق تشطيب 160.")
        }
    }

    /**
     * Atomic transaction restore — guarantees either full restore or zero corruption.
     */
    suspend fun restoreFromBackup(payload: BackupPayload) {
        payload.receiptImagesBase64.forEach { (fileName, b64) ->
            ReceiptImageStorage.writeReceiptFromBase64(appContext, fileName, b64)
        }
        database.withTransaction {
            expenseDao.deleteAll()
            contractorDao.deleteAll()
            categoryDao.deleteAll()
            categoryDao.insertAll(payload.categories)
            contractorDao.insertAll(payload.contractors)
            expenseDao.insertAll(payload.expenses)
        }
    }

    /**
     * Exports RFC-4180 CSV starting with UTF-8 BOM (\uFEFF) so Arabic opens cleanly in Excel.
     */
    suspend fun exportExpensesCsv(): String {
        val categories = categoryDao.getAllSnapshot().associateBy { it.id }
        val contractors = contractorDao.getAllSnapshot().associateBy { it.id }
        val expenses = expenseDao.getAllSnapshot()

        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.appendLine("التاريخ,البند,النوع,البيان,الكمية,سعر الوحدة (ج.م),الإجمالي (ج.م),طريقة الدفع,المقاول / الصنايعي")

        for (e in expenses) {
            val catName = categories[e.categoryId]?.name ?: "غير مصنف"
            val contractorName = e.contractorId?.let { contractors[it]?.name } ?: ""
            val typeLabel = ExpenseType.fromCode(e.expenseType).labelAr
            val paymentLabel = PaymentMethod.fromCode(e.paymentMethod).labelAr
            val qtyStr = e.quantity?.let { MoneyFormatter.formatQuantity(it) } ?: ""
            val unitStr = e.unitPricePiasters?.let { MoneyFormatter.piastersToCsvDecimal(it) } ?: ""
            val totalStr = MoneyFormatter.piastersToCsvDecimal(e.amountPiasters)

            sb.appendLine(
                listOf(
                    MoneyFormatter.formatIsoDate(e.dateMillis),
                    catName,
                    typeLabel,
                    e.description,
                    qtyStr,
                    unitStr,
                    totalStr,
                    paymentLabel,
                    contractorName
                ).joinToString(",") { csvEscape(it) }
            )
        }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        return if (needsQuotes) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    companion object {
        fun defaultCategories(): List<CategoryEntity> = listOf(
            CategoryEntity(
                id = 1L,
                name = "المباني والمحارة",
                description = "شامل القاطوع الفاصل بين الريسبشن والسفرة",
                budgetPiasters = 10_000_00L,
                iconKey = CategoryIconKey.MASONRY.key,
                sortOrder = 1,
                isDefault = true
            ),
            CategoryEntity(
                id = 2L,
                name = "تأسيس وتشطيب الكهرباء",
                description = "أسلاك، لوحة، وشوش، سبوتات، ليد",
                budgetPiasters = 98_500_00L,
                iconKey = CategoryIconKey.ELECTRICAL.key,
                sortOrder = 2,
                isDefault = true
            ),
            CategoryEntity(
                id = 3L,
                name = "السباكة والعزل والأطقم الصحية",
                description = "تأسيس سباكة، عزل حمامات ومطبخ، وأطقم صحية",
                budgetPiasters = 85_500_00L,
                iconKey = CategoryIconKey.PLUMBING.key,
                sortOrder = 3,
                isDefault = true
            ),
            CategoryEntity(
                id = 4L,
                name = "السيراميك والبورسلين والمون",
                description = "شراء السيراميك والبورسلين والأسمنت والرمل والمصنعية",
                budgetPiasters = 138_500_00L,
                iconKey = CategoryIconKey.CERAMIC.key,
                sortOrder = 4,
                isDefault = true
            ),
            CategoryEntity(
                id = 5L,
                name = "الجبس بورد والتهوية",
                description = "شامل شفاط السقف المركزي لحمام الماستر",
                budgetPiasters = 54_100_00L,
                iconKey = CategoryIconKey.GYPSUM.key,
                sortOrder = 5,
                isDefault = true
            ),
            CategoryEntity(
                id = 6L,
                name = "الدهانات والمعجون",
                description = "معجون، سيلر، بطانة، وتشطيب الدهانات",
                budgetPiasters = 53_000_00L,
                iconKey = CategoryIconKey.PAINT.key,
                sortOrder = 6,
                isDefault = true
            ),
            CategoryEntity(
                id = 7L,
                name = "النجارة والأبواب",
                description = "أبواب WPC للحمامات، الباب الجرار السطحي للدرسينج، ووحدة الماستر",
                budgetPiasters = 79_000_00L,
                iconKey = CategoryIconKey.CARPENTRY.key,
                sortOrder = 7,
                isDefault = true
            ),
            CategoryEntity(
                id = 8L,
                name = "الألوميتال / UPVC",
                description = "شبابيك وأبواب البلكونات والقطاعات العازلة",
                budgetPiasters = 64_800_00L,
                iconKey = CategoryIconKey.ALUMINUM.key,
                sortOrder = 8,
                isDefault = true
            )
        )
    }
}
