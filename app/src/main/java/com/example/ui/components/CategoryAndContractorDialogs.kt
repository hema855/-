package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.model.CategoryIconKey
import com.example.data.model.PaymentMethod
import com.example.data.repository.BackupPayload
import com.example.ui.state.ContractorSummary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.util.MoneyFormatter

@Composable
fun CategoryEditDialog(
    initialCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var description by remember { mutableStateOf(initialCategory?.description ?: "") }
    var budgetText by remember {
        mutableStateOf(
            initialCategory?.let { MoneyFormatter.piastersToEditableString(it.budgetPiasters) } ?: ""
        )
    }
    var iconKey by remember {
        mutableStateOf(initialCategory?.iconKey ?: CategoryIconKey.TOOLS.key)
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Text(
                text = if (initialCategory == null) "إضافة بند تشطيب جديد" else "تعديل البند والميزانية",
                style = MaterialTheme.typography.titleLarge,
                color = Slate900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("اسم البند") },
                    placeholder = { Text("مثال: التكييفات والتأسيس") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("التفاصيل / النطاق (اختياري)") },
                    placeholder = { Text("مثال: مواسير نحاس + حوامل + صرف") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it; errorMsg = null },
                    label = { Text("الميزانية التقديرية (ج.م)") },
                    placeholder = { Text("25000") },
                    suffix = { Text("ج.م", fontWeight = FontWeight.Bold, color = VolcanicOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_input")
                )

                Text(
                    text = "أيقونة البند",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate700
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(CategoryIconKey.entries) { iconItem ->
                        val selected = iconKey == iconItem.key
                        FilterChip(
                            selected = selected,
                            onClick = { iconKey = iconItem.key },
                            label = { Text(iconItem.labelAr, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = iconItem.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }

                if (initialCategory != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(initialCategory.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("حذف هذا البند (إذا كان فارغاً)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val parsedBudget = MoneyFormatter.parseEgpToPiasters(budgetText)
                    if (cleanName.isEmpty()) {
                        errorMsg = "يرجى إدخال اسم البند."
                        return@Button
                    }
                    if (parsedBudget == null || parsedBudget < 0L) {
                        errorMsg = "يرجى إدخال ميزانية تقديرية صحيحة."
                        return@Button
                    }
                    onSave(
                        CategoryEntity(
                            id = initialCategory?.id ?: 0L,
                            name = cleanName,
                            description = description.trim(),
                            budgetPiasters = parsedBudget,
                            iconKey = iconKey,
                            sortOrder = initialCategory?.sortOrder ?: 0,
                            isDefault = initialCategory?.isDefault ?: false
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange),
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Slate700)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorEditDialog(
    categories: List<CategoryEntity>,
    initialContractor: ContractorEntity?,
    onDismiss: () -> Unit,
    onSave: (ContractorEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialContractor?.name ?: "") }
    var trade by remember { mutableStateOf(initialContractor?.trade ?: "") }
    var selectedCategoryId by remember {
        mutableLongStateOf(initialContractor?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var agreedText by remember {
        mutableStateOf(
            initialContractor?.let { MoneyFormatter.piastersToEditableString(it.agreedAmountPiasters) } ?: ""
        )
    }
    var phone by remember { mutableStateOf(initialContractor?.phone ?: "") }
    var notes by remember { mutableStateOf(initialContractor?.notes ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val selectedCatName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "اختر البند"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Text(
                text = if (initialContractor == null) "إضافة مقاول / صنايعي" else "تعديل بيانات المقاول",
                style = MaterialTheme.typography.titleLarge,
                color = Slate900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("الاسم") },
                    placeholder = { Text("مثال: الأسطى محمود") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contractor_name_input")
                )
                OutlinedTextField(
                    value = trade,
                    onValueChange = { trade = it; errorMsg = null },
                    label = { Text("المهنة / التخصص") },
                    placeholder = { Text("مثال: كهربائي / سباك / نقاش") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contractor_trade_input")
                )
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("البند المرتبط (تُسجل عليه الدفعات)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = agreedText,
                    onValueChange = { agreedText = it; errorMsg = null },
                    label = { Text("إجمالي الاتفاق / المقطوعية (ج.م)") },
                    placeholder = { Text("18000") },
                    suffix = { Text("ج.م", fontWeight = FontWeight.Bold, color = VolcanicOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contractor_agreed_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (اختياري)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("تفاصيل الاتفاق / ملاحظات (اختياري)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanTrade = trade.trim()
                    val agreed = MoneyFormatter.parseEgpToPiasters(agreedText)
                    if (cleanName.isEmpty()) {
                        errorMsg = "يرجى إدخال اسم المقاول أو الصنايعي."
                        return@Button
                    }
                    if (cleanTrade.isEmpty()) {
                        errorMsg = "يرجى إدخال المهنة."
                        return@Button
                    }
                    if (agreed == null || agreed < 0L) {
                        errorMsg = "يرجى إدخال قيمة الاتفاق بشكل صحيح."
                        return@Button
                    }
                    onSave(
                        ContractorEntity(
                            id = initialContractor?.id ?: 0L,
                            name = cleanName,
                            trade = cleanTrade,
                            categoryId = selectedCategoryId,
                            agreedAmountPiasters = agreed,
                            phone = phone.trim(),
                            notes = notes.trim(),
                            createdAtMillis = initialContractor?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange),
                modifier = Modifier.testTag("save_contractor_button")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Slate700)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordContractorPaymentDialog(
    summary: ContractorSummary,
    onDismiss: () -> Unit,
    onConfirmPayment: (amountPiasters: Long, dateMillis: Long, method: PaymentMethod, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var dateMillis by remember { mutableLongStateOf(MoneyFormatter.todayStartMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val parsedAmount = MoneyFormatter.parseEgpToPiasters(amountText)
    val projectedRemaining = if (parsedAmount != null) {
        summary.remainingPiasters - parsedAmount
    } else {
        summary.remainingPiasters
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Column {
                Text(
                    text = "تسجيل دفعة — ${summary.contractor.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate900
                )
                Text(
                    text = "تُخصم من المتبقي وتُضاف تلقائياً كمصنعية في سجل المصروفات",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live balance summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المتبقي حالياً", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            Text(
                                text = MoneyFormatter.formatEgp(summary.remainingPiasters),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (summary.remainingPiasters < 0L) DangerRed else Slate900,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("المتبقي بعد الدفعة", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            Text(
                                text = MoneyFormatter.formatEgp(projectedRemaining),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (projectedRemaining < 0L) DangerRed else EmeraldSafe,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text("قيمة الدفعة (ج.م)") },
                    placeholder = { Text("2000") },
                    suffix = { Text("ج.م", fontWeight = FontWeight.Bold, color = VolcanicOrange) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contractor_payment_amount_input")
                )

                if (summary.remainingPiasters > 0L) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VolcanicOrangeContainer,
                        border = BorderStroke(1.dp, VolcanicOrange),
                        modifier = Modifier.clickable {
                            amountText = MoneyFormatter.piastersToEditableString(summary.remainingPiasters)
                            errorMsg = null
                        }
                    ) {
                        Text(
                            text = "سداد المتبقي بالكامل (${MoneyFormatter.formatEgp(summary.remainingPiasters)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = VolcanicOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Payment method chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val selected = selectedMethod == method
                        FilterChip(
                            selected = selected,
                            onClick = { selectedMethod = method },
                            label = {
                                Text(
                                    text = method.labelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VolcanicOrange,
                                selectedLabelColor = PureWhite,
                                selectedLeadingIconColor = PureWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Date row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate300),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تاريخ الدفعة", style = MaterialTheme.typography.labelMedium, color = Slate600)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = MoneyFormatter.formatDate(dateMillis),
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate900
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("ملاحظة (مثال: دفعة تحت الحساب / تشطيب)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (projectedRemaining < 0L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DangerRedContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "تنبيه: هذه الدفعة ستتجاوز إجمالي الاتفاق المتفق عليه.",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = MoneyFormatter.parseEgpToPiasters(amountText)
                    if (amount == null || amount <= 0L) {
                        errorMsg = "يرجى إدخال قيمة دفعة أكبر من صفر."
                        return@Button
                    }
                    onConfirmPayment(amount, dateMillis, selectedMethod, noteText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange),
                modifier = Modifier.testTag("confirm_contractor_payment_button")
            ) {
                Text("تأكيد الدفعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Slate700)
            }
        }
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }
                ) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun RestoreBackupConfirmDialog(
    payload: BackupPayload,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        icon = {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = VolcanicOrange,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "تأكيد استعادة النسخة الاحتياطية",
                style = MaterialTheme.typography.titleLarge,
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "سيتم استبدال البيانات الحالية بالكامل ببيانات النسخة الاحتياطية التالية:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("• تاريخ النسخة: ${MoneyFormatter.formatDate(payload.exportedAtMillis)}", style = MaterialTheme.typography.bodyMedium)
                        Text("• عدد البنود: ${payload.categories.size} بند", style = MaterialTheme.typography.bodyMedium)
                        Text("• عدد المصروفات: ${payload.expenses.size} مصروف", style = MaterialTheme.typography.bodyMedium)
                        Text("• عدد المقاولين: ${payload.contractors.size} مقاول", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "إجمالي المصروف في النسخة: ${MoneyFormatter.formatEgp(payload.totalSpentPiasters)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = VolcanicOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange),
                modifier = Modifier.testTag("confirm_restore_backup_button")
            ) {
                Text("استعادة البيانات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Slate700)
            }
        }
    )
}
