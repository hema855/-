package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.ExpenseEntity
import com.example.data.model.CategoryIconKey
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.ui.components.ReceiptViewerDialog
import com.example.ui.state.ExpenseRowItem
import com.example.ui.state.TashteebUiState
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.util.MoneyFormatter
import com.example.util.ReceiptImageStorage

@Composable
fun ExpensesScreen(
    uiState: TashteebUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectType: (ExpenseType?) -> Unit,
    onSelectPayment: (PaymentMethod?) -> Unit,
    onToggleOnlyWithReceipt: () -> Unit,
    onClearFilters: () -> Unit,
    onQuickAdd: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expenseToDelete by remember { mutableStateOf<ExpenseRowItem?>(null) }
    var viewingReceiptRow by remember { mutableStateOf<ExpenseRowItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Sticky Search & Filter Header
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.filterState.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expenses_search_input"),
                    placeholder = { Text("ابحث بالبيان، البند، أو اسم المقاول...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "بحث",
                            tint = Slate600
                        )
                    },
                    trailingIcon = {
                        if (uiState.filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "مسح البحث",
                                    tint = Slate600
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50,
                        focusedBorderColor = VolcanicOrange,
                        unfocusedBorderColor = Slate300
                    )
                )

                // Type Filter Row (الكل / خامات / مصنعية / نقل ومشال / نثريات)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.selectedType == null,
                            onClick = { onSelectType(null) },
                            label = { Text("كل الأنواع", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate700,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                    items(ExpenseType.entries) { type ->
                        val selected = uiState.filterState.selectedType == type
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectType(if (selected) null else type) },
                            label = { Text(type.labelAr, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = type.accentColor,
                                selectedLabelColor = PureWhite,
                                selectedLeadingIconColor = PureWhite
                            ),
                            modifier = Modifier.testTag("filter_type_${type.code}")
                        )
                    }
                }

                // Category Filter Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.selectedCategoryId == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("كل البنود", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VolcanicOrange,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                    items(uiState.categorySummaries, key = { it.category.id }) { catSum ->
                        val selected = uiState.filterState.selectedCategoryId == catSum.category.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(if (selected) null else catSum.category.id) },
                            label = {
                                Text(
                                    text = catSum.category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VolcanicOrange,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }

                // Payment Method & Receipt Filter Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyWithReceipt,
                            onClick = onToggleOnlyWithReceipt,
                            label = { Text("بصورة فاتورة", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VolcanicOrange,
                                selectedLabelColor = PureWhite,
                                selectedLeadingIconColor = PureWhite
                            ),
                            modifier = Modifier.testTag("filter_with_receipt")
                        )
                    }
                    items(PaymentMethod.entries) { method ->
                        val selected = uiState.filterState.selectedPaymentMethod == method
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectPayment(if (selected) null else method) },
                            label = { Text(method.labelAr, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = method.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate700,
                                selectedLabelColor = PureWhite,
                                selectedLeadingIconColor = VolcanicOrange
                            )
                        )
                    }
                }
            }
        }

        // Instant Live Filtered Total Bar
        Surface(
            color = Slate700,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (uiState.filterState.hasActiveFilters) {
                            "إجمالي النتائج (${uiState.filteredExpenseRows.size}):"
                        } else {
                            "إجمالي السجل (${uiState.filteredExpenseRows.size} حركة):"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate200
                    )
                    if (uiState.filterState.hasActiveFilters) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VolcanicOrange,
                            modifier = Modifier.testTag("clear_filters_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterAltOff,
                                    contentDescription = "مسح الفلاتر",
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                TextButton(
                                    onClick = onClearFilters,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(20.dp)
                                ) {
                                    Text(
                                        text = "مسح",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = MoneyFormatter.formatEgp(uiState.filteredTotalPiasters),
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("filtered_total_text")
                )
            }
        }

        // Expenses List or Clean Empty State
        if (uiState.filteredExpenseRows.isEmpty()) {
            EmptyExpensesState(
                hasFilters = uiState.filterState.hasActiveFilters,
                onClearFilters = onClearFilters,
                onQuickAdd = onQuickAdd
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filteredExpenseRows, key = { it.expense.id }) { row ->
                    ExpenseLedgerCard(
                        item = row,
                        onEdit = { onEditExpense(row.expense) },
                        onDelete = { expenseToDelete = row },
                        onViewReceipt = { viewingReceiptRow = row }
                    )
                }
            }
        }
    }

    viewingReceiptRow?.let { row ->
        val path = row.expense.receiptImagePath
        if (path != null) {
            ReceiptViewerDialog(
                receiptFileName = path,
                title = row.expense.description,
                subtitle = "${row.categoryName} • ${MoneyFormatter.formatDate(row.expense.dateMillis)}",
                amountText = MoneyFormatter.formatEgp(row.expense.amountPiasters),
                onDismiss = { viewingReceiptRow = null }
            )
        }
    }

    expenseToDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = PureWhite,
            title = { Text("حذف المصروف؟", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "سيتم حذف \"${row.expense.description}\" بقيمة ${MoneyFormatter.formatEgp(row.expense.amountPiasters)} وتحديث الميزانية وحساب المقاول تلقائياً.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(row.expense.id)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("إلغاء", color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun ExpenseLedgerCard(
    item: ExpenseRowItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val context = LocalContext.current
    val type = ExpenseType.fromCode(item.expense.expenseType)
    val payment = PaymentMethod.fromCode(item.expense.paymentMethod)
    val catIcon = CategoryIconKey.fromKey(item.categoryIconKey).icon
    val receiptFile = remember(item.expense.receiptImagePath) {
        ReceiptImageStorage.resolveReceiptFile(context, item.expense.receiptImagePath)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_row_${item.expense.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Top meta pills: Type + Category
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = type.containerColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = type.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = type.labelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = type.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate100
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = Slate700,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = item.expense.description,
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.expense.quantity != null && item.expense.unitPricePiasters != null) {
                        Text(
                            text = "الكمية: ${MoneyFormatter.formatQuantity(item.expense.quantity)} × ${MoneyFormatter.formatEgp(item.expense.unitPricePiasters)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MoneyFormatter.formatEgp(item.expense.amountPiasters),
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = MoneyFormatter.formatDate(item.expense.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // Receipt Thumbnail Strip (if invoice photo attached)
            if (receiptFile != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewReceipt)
                        .testTag("expense_receipt_thumb_${item.expense.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = receiptFile,
                            contentDescription = "صورة الفاتورة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate200)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "صورة الفاتورة مرفقة",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate900,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اضغط لعرض وتكبير الفاتورة",
                                style = MaterialTheme.typography.labelSmall,
                                color = VolcanicOrange
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Receipt,
                            contentDescription = null,
                            tint = VolcanicOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom meta + actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = payment.icon,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = payment.labelAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }

                    if (item.contractorName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = VolcanicOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = item.contractorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = VolcanicOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "تعديل المصروف",
                            tint = Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف المصروف",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyExpensesState(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (hasFilters) Slate200 else VolcanicOrangeContainer,
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ReceiptLong,
                    contentDescription = null,
                    tint = if (hasFilters) Slate700 else VolcanicOrange,
                    modifier = Modifier.size(38.dp)
                )
            }
            Text(
                text = if (hasFilters) "لا توجد مصروفات تطابق الفلتر الحالي" else "سجل المصروفات جاهز للبدء",
                style = MaterialTheme.typography.titleMedium,
                color = Slate900,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasFilters) {
                    "جرّب تغيير كلمات البحث أو مسح الفلاتر لعرض جميع الحركات."
                } else {
                    "تم تجهيز البنود الـ 8 وميزانياتها التقديرية بدون أي بيانات وهمية. سجّل أول فاتورة في أقل من 5 ثوانٍ."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Slate600,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (hasFilters) {
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                ) {
                    Text("عرض كل المصروفات")
                }
            } else {
                Button(
                    onClick = onQuickAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange),
                    modifier = Modifier.testTag("empty_add_first_expense_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة أول مصروف")
                }
            }
        }
    }
}
