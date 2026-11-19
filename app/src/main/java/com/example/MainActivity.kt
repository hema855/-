package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.repository.TashteebRepository
import com.example.ui.components.CategoryEditDialog
import com.example.ui.components.ContractorEditDialog
import com.example.ui.components.QuickAddExpenseSheet
import com.example.ui.components.RecordContractorPaymentDialog
import com.example.ui.components.RestoreBackupConfirmDialog
import com.example.ui.screens.ContractorsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.ReportsBackupScreen
import com.example.ui.state.AppTab
import com.example.ui.state.ContractorSummary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.TashteebTheme
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.ui.viewmodel.TashteebViewModel
import com.example.util.MoneyFormatter
import com.example.util.ReportPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Slate50.toArgb(),
                Slate50.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                PureWhite.toArgb(),
                PureWhite.toArgb()
            )
        )

        val database = AppDatabase.getInstance(applicationContext)
        val repository = TashteebRepository(database, applicationContext)

        setContent {
            TashteebTheme {
                val vm: TashteebViewModel = viewModel(
                    factory = TashteebViewModel.Factory(repository)
                )
                TashteebAppShell(viewModel = vm)
            }
        }
    }
}

@Composable
private fun TashteebAppShell(viewModel: TashteebViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD) }

    // Modal state holders
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var quickAddPreselectedCategoryId by remember { mutableStateOf<Long?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var showContractorDialog by remember { mutableStateOf(false) }
    var editingContractor by remember { mutableStateOf<ContractorEntity?>(null) }
    var paymentTargetContractor by remember { mutableStateOf<ContractorSummary?>(null) }

    // Collect one-shot messages
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back navigation from secondary tabs returns cleanly to Dashboard
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    // SAF Zero-Permission Export JSON Launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = viewModel.generateJsonBackup()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    viewModel.showMessage("تم حفظ النسخة الاحتياطية (JSON) بنجاح")
                } catch (_: Exception) {
                    viewModel.showMessage("تعذر حفظ ملف النسخة الاحتياطية")
                }
            }
        }
    }

    // SAF Zero-Permission Import JSON Launcher
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                            it.readText()
                        }
                    }
                    if (content != null) {
                        viewModel.inspectBackupJson(content)
                    } else {
                        viewModel.showMessage("تعذر قراءة الملف المختار")
                    }
                } catch (_: Exception) {
                    viewModel.showMessage("حدث خطأ أثناء فتح الملف")
                }
            }
        }
    }

    // SAF Zero-Permission Export CSV Launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csv = viewModel.generateCsvReport()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(csv.toByteArray(Charsets.UTF_8))
                        }
                    }
                    viewModel.showMessage("تم تصدير تقرير المصروفات (CSV لإكسل) بنجاح")
                } catch (_: Exception) {
                    viewModel.showMessage("تعذر تصدير ملف CSV")
                }
            }
        }
    }

    val openQuickAdd: (Long?) -> Unit = { catId ->
        editingExpense = null
        quickAddPreselectedCategoryId = catId
        showQuickAddSheet = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Slate50,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.EXPENSES) {
                    ExtendedFloatingActionButton(
                        onClick = { openQuickAdd(uiState.filterState.selectedCategoryId) },
                        containerColor = VolcanicOrange,
                        contentColor = PureWhite,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "إضافة مصروف سريع"
                            )
                        },
                        text = {
                            Text(
                                text = "مصروف سريع",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("fab_quick_add_expense")
                    )
                }
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 3.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.labelAr
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.labelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VolcanicOrange,
                                    selectedTextColor = VolcanicOrange,
                                    indicatorColor = VolcanicOrangeContainer,
                                    unselectedIconColor = Slate600,
                                    unselectedTextColor = Slate600
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = PureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.labelAr
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.labelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = VolcanicOrange,
                                    selectedTextColor = VolcanicOrange,
                                    indicatorColor = VolcanicOrangeContainer,
                                    unselectedIconColor = Slate600,
                                    unselectedTextColor = Slate600
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 1.dp, max = 1.dp)
                            .background(Slate200)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 960.dp)

                    when (currentTab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            uiState = uiState,
                            onAddCategory = {
                                editingCategory = null
                                showCategoryDialog = true
                            },
                            onEditCategory = { cat ->
                                editingCategory = cat
                                showCategoryDialog = true
                            },
                            onOpenCategoryExpenses = { catId ->
                                viewModel.clearFilters()
                                viewModel.selectCategoryFilter(catId)
                                currentTab = AppTab.EXPENSES
                            },
                            onQuickAddForCategory = { catId -> openQuickAdd(catId) },
                            modifier = contentModifier
                        )

                        AppTab.EXPENSES -> ExpensesScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearchQuery,
                            onSelectCategory = viewModel::selectCategoryFilter,
                            onSelectType = viewModel::selectTypeFilter,
                            onSelectPayment = viewModel::selectPaymentFilter,
                            onToggleOnlyWithReceipt = viewModel::toggleOnlyWithReceipt,
                            onClearFilters = viewModel::clearFilters,
                            onQuickAdd = { openQuickAdd(uiState.filterState.selectedCategoryId) },
                            onEditExpense = { expense ->
                                editingExpense = expense
                                quickAddPreselectedCategoryId = expense.categoryId
                                showQuickAddSheet = true
                            },
                            onDeleteExpense = viewModel::deleteExpense,
                            modifier = contentModifier
                        )

                        AppTab.CONTRACTORS -> ContractorsScreen(
                            uiState = uiState,
                            onAddContractor = {
                                editingContractor = null
                                showContractorDialog = true
                            },
                            onEditContractor = { contractor ->
                                editingContractor = contractor
                                showContractorDialog = true
                            },
                            onDeleteContractor = viewModel::deleteContractor,
                            onRecordPayment = { summary ->
                                paymentTargetContractor = summary
                            },
                            onEditPaymentExpense = { expense ->
                                editingExpense = expense
                                quickAddPreselectedCategoryId = expense.categoryId
                                showQuickAddSheet = true
                            },
                            onDeletePaymentExpense = viewModel::deleteExpense,
                            modifier = contentModifier
                        )

                        AppTab.REPORTS -> ReportsBackupScreen(
                            uiState = uiState,
                            onExportJsonBackup = {
                                exportJsonLauncher.launch("tashteeb160_backup_${MoneyFormatter.formatFileTimestamp()}.json")
                            },
                            onImportJsonBackup = {
                                importJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            onExportCsv = {
                                exportCsvLauncher.launch("tashteeb160_expenses_${MoneyFormatter.formatFileTimestamp()}.csv")
                            },
                            onPrintReport = {
                                ReportPrinter.printFinancialReport(
                                    context = context,
                                    totalBudgetPiasters = uiState.totalBudgetPiasters,
                                    totalSpentPiasters = uiState.totalSpentPiasters,
                                    remainingPiasters = uiState.remainingPiasters,
                                    completionPercent = uiState.completionPercent,
                                    categories = uiState.categorySummaries,
                                    contractors = uiState.contractorSummaries,
                                    expenses = uiState.allExpenseRows,
                                    materialsPiasters = uiState.materialsPiasters,
                                    laborPiasters = uiState.laborPiasters,
                                    transportPiasters = uiState.transportPiasters,
                                    miscPiasters = uiState.miscPiasters
                                )
                            },
                            onAddCategory = {
                                editingCategory = null
                                showCategoryDialog = true
                            },
                            onEditCategory = { cat ->
                                editingCategory = cat
                                showCategoryDialog = true
                            },
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }

    // Quick Add / Edit Expense Modal Sheet
    if (showQuickAddSheet) {
        QuickAddExpenseSheet(
            categories = uiState.categorySummaries.map { it.category },
            contractors = uiState.contractorSummaries.map { it.contractor },
            initialExpense = editingExpense,
            preselectedCategoryId = quickAddPreselectedCategoryId,
            onDismiss = {
                showQuickAddSheet = false
                editingExpense = null
            },
            onSave = { expense ->
                viewModel.saveExpense(expense)
                showQuickAddSheet = false
                editingExpense = null
            }
        )
    }

    // Add / Edit Category Dialog
    if (showCategoryDialog) {
        CategoryEditDialog(
            initialCategory = editingCategory,
            onDismiss = {
                showCategoryDialog = false
                editingCategory = null
            },
            onSave = { category ->
                viewModel.saveCategory(category)
                showCategoryDialog = false
                editingCategory = null
            },
            onDelete = { catId ->
                viewModel.deleteCategory(catId)
                showCategoryDialog = false
                editingCategory = null
            }
        )
    }

    // Add / Edit Contractor Dialog
    if (showContractorDialog) {
        ContractorEditDialog(
            categories = uiState.categorySummaries.map { it.category },
            initialContractor = editingContractor,
            onDismiss = {
                showContractorDialog = false
                editingContractor = null
            },
            onSave = { contractor ->
                viewModel.saveContractor(contractor)
                showContractorDialog = false
                editingContractor = null
            }
        )
    }

    // Record Contractor Payment Dialog
    paymentTargetContractor?.let { target ->
        RecordContractorPaymentDialog(
            summary = target,
            onDismiss = { paymentTargetContractor = null },
            onConfirmPayment = { amount, dateMillis, method, note ->
                viewModel.recordContractorPayment(
                    contractor = target.contractor,
                    amountPiasters = amount,
                    dateMillis = dateMillis,
                    paymentMethod = method,
                    note = note
                )
                paymentTargetContractor = null
            }
        )
    }

    // Confirm Restore JSON Backup Dialog
    uiState.pendingRestorePreview?.let { payload ->
        RestoreBackupConfirmDialog(
            payload = payload,
            onDismiss = viewModel::dismissRestorePreview,
            onConfirm = viewModel::confirmRestoreBackup
        )
    }
}

private fun tabIcon(tab: AppTab, selected: Boolean): ImageVector = when (tab) {
    AppTab.DASHBOARD -> if (selected) Icons.Rounded.Dashboard else Icons.Outlined.Dashboard
    AppTab.EXPENSES -> if (selected) Icons.Rounded.ReceiptLong else Icons.Outlined.ReceiptLong
    AppTab.CONTRACTORS -> if (selected) Icons.Rounded.Engineering else Icons.Outlined.Engineering
    AppTab.REPORTS -> if (selected) Icons.Rounded.Assessment else Icons.Outlined.Assessment
}
