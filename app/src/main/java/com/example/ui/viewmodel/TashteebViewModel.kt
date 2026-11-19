package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.data.repository.BackupParseResult
import com.example.data.repository.BackupPayload
import com.example.data.repository.TashteebRepository
import com.example.ui.state.CategoryBudgetSummary
import com.example.ui.state.ContractorSummary
import com.example.ui.state.ExpenseFilterState
import com.example.ui.state.ExpenseRowItem
import com.example.ui.state.TashteebUiState
import com.example.util.MoneyFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TashteebViewModel(
    private val repository: TashteebRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(ExpenseFilterState())
    private val pendingRestorePreview = MutableStateFlow<BackupPayload?>(null)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<TashteebUiState> = combine(
        repository.categoriesFlow,
        repository.contractorsFlow,
        repository.expensesFlow,
        filterState,
        pendingRestorePreview
    ) { categories, contractors, expenses, filters, restorePreview ->
        val categoryMap = categories.associateBy { it.id }
        val contractorMap = contractors.associateBy { it.id }

        val allExpenseRows = expenses.map { e ->
            val cat = categoryMap[e.categoryId]
            val contractor = e.contractorId?.let { contractorMap[it] }
            ExpenseRowItem(
                expense = e,
                categoryName = cat?.name ?: "غير مصنف",
                categoryIconKey = cat?.iconKey ?: "CUSTOM",
                contractorName = contractor?.name
            )
        }

        val expensesByCategory = expenses.groupBy { it.categoryId }
        val categorySummaries = categories.map { cat ->
            val catExpenses = expensesByCategory[cat.id].orEmpty()
            val spent = catExpenses.sumOf { it.amountPiasters }
            val remaining = cat.budgetPiasters - spent
            val percent = MoneyFormatter.computePercentage(spent, cat.budgetPiasters)
            CategoryBudgetSummary(
                category = cat,
                spentPiasters = spent,
                remainingPiasters = remaining,
                percentUsed = percent,
                expenseCount = catExpenses.size
            )
        }

        val totalBudget = categories.sumOf { it.budgetPiasters }
        val totalSpent = expenses.sumOf { it.amountPiasters }
        val totalRemaining = totalBudget - totalSpent
        val completionPercent = MoneyFormatter.computePercentage(totalSpent, totalBudget)

        val materialsTotal = expenses
            .filter { it.expenseType == ExpenseType.MATERIALS.code }
            .sumOf { it.amountPiasters }
        val laborTotal = expenses
            .filter { it.expenseType == ExpenseType.LABOR.code }
            .sumOf { it.amountPiasters }
        val transportTotal = expenses
            .filter { it.expenseType == ExpenseType.TRANSPORT.code }
            .sumOf { it.amountPiasters }
        val miscTotal = expenses
            .filter { it.expenseType == ExpenseType.MISC.code }
            .sumOf { it.amountPiasters }

        val query = filters.searchQuery.trim()
        val filteredRows = allExpenseRows.filter { row ->
            val matchesQuery = query.isEmpty() ||
                row.expense.description.contains(query, ignoreCase = true) ||
                row.categoryName.contains(query, ignoreCase = true) ||
                (row.contractorName?.contains(query, ignoreCase = true) == true)
            val matchesCategory = filters.selectedCategoryId == null ||
                row.expense.categoryId == filters.selectedCategoryId
            val matchesType = filters.selectedType == null ||
                row.expense.expenseType == filters.selectedType.code
            val matchesPayment = filters.selectedPaymentMethod == null ||
                row.expense.paymentMethod == filters.selectedPaymentMethod.code
            val matchesReceipt = !filters.onlyWithReceipt ||
                !row.expense.receiptImagePath.isNullOrBlank()

            matchesQuery && matchesCategory && matchesType && matchesPayment && matchesReceipt
        }
        val filteredTotal = filteredRows.sumOf { it.expense.amountPiasters }

        val rowsByContractor = allExpenseRows
            .filter { it.expense.contractorId != null && it.expense.countsTowardAgreement }
            .groupBy { it.expense.contractorId!! }

        val contractorSummaries = contractors.map { contractor ->
            val payments = rowsByContractor[contractor.id].orEmpty()
            val paid = payments.sumOf { it.expense.amountPiasters }
            val remaining = contractor.agreedAmountPiasters - paid
            val percent = MoneyFormatter.computePercentage(paid, contractor.agreedAmountPiasters)
            ContractorSummary(
                contractor = contractor,
                categoryName = categoryMap[contractor.categoryId]?.name ?: "غير مصنف",
                paidPiasters = paid,
                remainingPiasters = remaining,
                percentPaid = percent,
                payments = payments
            )
        }

        val totalAgreed = contractorSummaries.sumOf { it.contractor.agreedAmountPiasters }
        val totalPaidContractors = contractorSummaries.sumOf { it.paidPiasters }
        val totalRemainingContractors = contractorSummaries.sumOf { it.remainingPiasters }

        TashteebUiState(
            isReady = categories.isNotEmpty(),
            totalBudgetPiasters = totalBudget,
            totalSpentPiasters = totalSpent,
            remainingPiasters = totalRemaining,
            completionPercent = completionPercent,
            materialsPiasters = materialsTotal,
            laborPiasters = laborTotal,
            transportPiasters = transportTotal,
            miscPiasters = miscTotal,
            overBudgetCategoriesCount = categorySummaries.count { it.isOverBudget },
            categorySummaries = categorySummaries,
            allExpenseRows = allExpenseRows,
            filteredExpenseRows = filteredRows,
            filteredTotalPiasters = filteredTotal,
            contractorSummaries = contractorSummaries,
            totalContractorsAgreedPiasters = totalAgreed,
            totalContractorsPaidPiasters = totalPaidContractors,
            totalContractorsRemainingPiasters = totalRemainingContractors,
            filterState = filters,
            pendingRestorePreview = restorePreview
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TashteebUiState()
    )

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun selectCategoryFilter(categoryId: Long?) {
        filterState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun selectTypeFilter(type: ExpenseType?) {
        filterState.update { it.copy(selectedType = type) }
    }

    fun selectPaymentFilter(method: PaymentMethod?) {
        filterState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun toggleOnlyWithReceipt() {
        filterState.update { it.copy(onlyWithReceipt = !it.onlyWithReceipt) }
    }

    fun clearFilters() {
        filterState.value = ExpenseFilterState()
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val isNew = expense.id == 0L
            repository.saveExpense(expense)
            _events.tryEmit(if (isNew) "تم تسجيل المصروف بنجاح" else "تم تحديث المصروف بنجاح")
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            _events.tryEmit("تم حذف المصروف وتحديث الإجماليات")
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val isNew = category.id == 0L
            val nextSort = if (isNew) {
                (uiState.value.categorySummaries.maxOfOrNull { it.category.sortOrder } ?: 0) + 1
            } else {
                category.sortOrder
            }
            repository.saveCategory(category.copy(sortOrder = nextSort))
            _events.tryEmit(if (isNew) "تم إضافة البند الجديد" else "تم تعديل البند والميزانية")
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val deleted = repository.deleteCategoryIfEmpty(categoryId)
            if (deleted) {
                if (filterState.value.selectedCategoryId == categoryId) {
                    filterState.update { it.copy(selectedCategoryId = null) }
                }
                _events.tryEmit("تم حذف البند بنجاح")
            } else {
                _events.tryEmit("لا يمكن حذف بند يحتوي على مصروفات مسجلة. احذف أو انقل مصروفاته أولاً.")
            }
        }
    }

    fun saveContractor(contractor: ContractorEntity) {
        viewModelScope.launch {
            val isNew = contractor.id == 0L
            repository.saveContractor(contractor)
            _events.tryEmit(if (isNew) "تم تسجيل المقاول / الصنايعي بنجاح" else "تم تحديث بيانات المقاول")
        }
    }

    fun deleteContractor(contractorId: Long) {
        viewModelScope.launch {
            repository.deleteContractor(contractorId)
            _events.tryEmit("تم حذف حساب المقاول (مع الاحتفاظ بالمصروفات التاريخية في السجل)")
        }
    }

    fun recordContractorPayment(
        contractor: ContractorEntity,
        amountPiasters: Long,
        dateMillis: Long,
        paymentMethod: PaymentMethod,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordContractorPayment(
                contractor = contractor,
                amountPiasters = amountPiasters,
                dateMillis = dateMillis,
                paymentMethod = paymentMethod,
                note = note
            )
            _events.tryEmit("تم تسجيل الدفعة وخصمها وإضافتها تلقائياً لسجل المصروفات")
        }
    }

    suspend fun generateJsonBackup(): String = repository.exportJsonBackup()

    suspend fun generateCsvReport(): String = repository.exportExpensesCsv()

    fun inspectBackupJson(rawJson: String) {
        when (val result = repository.parseBackupJson(rawJson)) {
            is BackupParseResult.Valid -> {
                pendingRestorePreview.value = result.payload
            }
            is BackupParseResult.Error -> {
                _events.tryEmit(result.messageAr)
            }
        }
    }

    fun dismissRestorePreview() {
        pendingRestorePreview.value = null
    }

    fun confirmRestoreBackup() {
        val payload = pendingRestorePreview.value ?: return
        viewModelScope.launch {
            repository.restoreFromBackup(payload)
            pendingRestorePreview.value = null
            filterState.value = ExpenseFilterState()
            _events.tryEmit("تم استعادة النسخة الاحتياطية بنجاح (${payload.expenses.size} مصروف)")
        }
    }

    fun showMessage(msg: String) {
        _events.tryEmit(msg)
    }

    class Factory(private val repository: TashteebRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TashteebViewModel(repository) as T
        }
    }
}
