package com.example.ui.state

import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.data.repository.BackupPayload

enum class AppTab(val route: String, val labelAr: String) {
    DASHBOARD("dashboard", "الرئيسية"),
    EXPENSES("expenses", "المصروفات"),
    CONTRACTORS("contractors", "المقاولون"),
    REPORTS("reports", "التقارير")
}

data class CategoryBudgetSummary(
    val category: CategoryEntity,
    val spentPiasters: Long,
    val remainingPiasters: Long,
    val percentUsed: Float,
    val expenseCount: Int
) {
    val isOverBudget: Boolean get() = spentPiasters > category.budgetPiasters && category.budgetPiasters > 0L || (category.budgetPiasters == 0L && spentPiasters > 0L)
    val isWarning: Boolean get() = !isOverBudget && percentUsed >= 85f
}

data class ExpenseRowItem(
    val expense: ExpenseEntity,
    val categoryName: String,
    val categoryIconKey: String,
    val contractorName: String?
)

data class ContractorSummary(
    val contractor: ContractorEntity,
    val categoryName: String,
    val paidPiasters: Long,
    val remainingPiasters: Long,
    val percentPaid: Float,
    val payments: List<ExpenseRowItem>
) {
    val isOverPaid: Boolean get() = paidPiasters > contractor.agreedAmountPiasters && contractor.agreedAmountPiasters > 0L
    val isSettled: Boolean get() = contractor.agreedAmountPiasters > 0L && paidPiasters == contractor.agreedAmountPiasters
}

data class ExpenseFilterState(
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val selectedType: ExpenseType? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val onlyWithReceipt: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
            selectedCategoryId != null ||
            selectedType != null ||
            selectedPaymentMethod != null ||
            onlyWithReceipt
}

data class TashteebUiState(
    val isReady: Boolean = false,
    val totalBudgetPiasters: Long = 0L,
    val totalSpentPiasters: Long = 0L,
    val remainingPiasters: Long = 0L,
    val completionPercent: Float = 0f,
    val materialsPiasters: Long = 0L,
    val laborPiasters: Long = 0L,
    val transportPiasters: Long = 0L,
    val miscPiasters: Long = 0L,
    val overBudgetCategoriesCount: Int = 0,
    val categorySummaries: List<CategoryBudgetSummary> = emptyList(),
    val allExpenseRows: List<ExpenseRowItem> = emptyList(),
    val filteredExpenseRows: List<ExpenseRowItem> = emptyList(),
    val filteredTotalPiasters: Long = 0L,
    val contractorSummaries: List<ContractorSummary> = emptyList(),
    val totalContractorsAgreedPiasters: Long = 0L,
    val totalContractorsPaidPiasters: Long = 0L,
    val totalContractorsRemainingPiasters: Long = 0L,
    val filterState: ExpenseFilterState = ExpenseFilterState(),
    val pendingRestorePreview: BackupPayload? = null
)
