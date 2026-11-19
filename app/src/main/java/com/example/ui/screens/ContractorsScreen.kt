package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.ui.state.ContractorSummary
import com.example.ui.state.TashteebUiState
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBorder
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.util.MoneyFormatter

@Composable
fun ContractorsScreen(
    uiState: TashteebUiState,
    onAddContractor: () -> Unit,
    onEditContractor: (ContractorEntity) -> Unit,
    onDeleteContractor: (Long) -> Unit,
    onRecordPayment: (ContractorSummary) -> Unit,
    onEditPaymentExpense: (ExpenseEntity) -> Unit,
    onDeletePaymentExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var contractorToDelete by remember { mutableStateOf<ContractorSummary?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Contractors Financial Summary Banner
        item(key = "contractors_summary") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حسابات المقاولين والصنايعية",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الدفعات تُخصم تلقائياً وتُسجل كمصنعية بدون تكرار",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContractorStatMini(
                            label = "إجمالي الاتفاقات",
                            value = MoneyFormatter.formatEgp(uiState.totalContractorsAgreedPiasters),
                            valueColor = PureWhite,
                            modifier = Modifier.weight(1f)
                        )
                        ContractorStatMini(
                            label = "المدفوع حتى الآن",
                            value = MoneyFormatter.formatEgp(uiState.totalContractorsPaidPiasters),
                            valueColor = VolcanicOrange,
                            modifier = Modifier.weight(1f)
                        )
                        ContractorStatMini(
                            label = "المتبقي لهم",
                            value = MoneyFormatter.formatEgp(uiState.totalContractorsRemainingPiasters),
                            valueColor = if (uiState.totalContractorsRemainingPiasters < 0L) DangerRed else EmeraldSafe,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = onAddContractor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_contractor_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إضافة مقاول أو صنايعي جديد",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Empty state or Contractor cards
        if (uiState.contractorSummaries.isEmpty()) {
            item(key = "contractors_empty") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(VolcanicOrangeContainer, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Engineering,
                                contentDescription = null,
                                tint = VolcanicOrange,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Text(
                            text = "لا يوجد مقاولون مسجلون حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أضف الكهربائي، السباك، النقاش، أو مقاول السيراميك مع قيمة المقطوعية المتفق عليها، وسجّل كل دفعة بضغطة زر.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.contractorSummaries, key = { it.contractor.id }) { summary ->
                ContractorLedgerCard(
                    summary = summary,
                    onRecordPayment = { onRecordPayment(summary) },
                    onEdit = { onEditContractor(summary.contractor) },
                    onDelete = { contractorToDelete = summary },
                    onEditPayment = onEditPaymentExpense,
                    onDeletePayment = onDeletePaymentExpense
                )
            }
        }
    }

    contractorToDelete?.let { summary ->
        AlertDialog(
            onDismissRequest = { contractorToDelete = null },
            containerColor = PureWhite,
            title = { Text("حذف حساب المقاول؟", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "سيتم حذف حساب \"${summary.contractor.name}\" (${summary.contractor.trade}). ستبقى جميع الدفعات السابقة محفوظة في سجل المصروفات العام للحفاظ على دقة الميزانية.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteContractor(summary.contractor.id)
                        contractorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("حذف الحساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { contractorToDelete = null }) {
                    Text("إلغاء", color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun ContractorStatMini(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Slate700
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate300,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ContractorLedgerCard(
    summary: ContractorSummary,
    onRecordPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditPayment: (ExpenseEntity) -> Unit,
    onDeletePayment: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val barColor = when {
        summary.isOverPaid -> DangerRed
        summary.isSettled -> EmeraldSafe
        else -> VolcanicOrange
    }
    val progressFraction = (summary.percentPaid / 100f).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(if (summary.isOverPaid) 1.5.dp else 1.dp, if (summary.isOverPaid) DangerRedBorder else Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contractor_card_${summary.contractor.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Slate100, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Engineering,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = summary.contractor.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${summary.contractor.trade} • ${summary.categoryName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                        if (summary.contractor.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phone,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = summary.contractor.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "تعديل المقاول",
                            tint = Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف المقاول",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Clear 3-column Ledger Numbers (إجمالي الاتفاق | المدفوع حتى الآن | المتبقي له)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate50, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إجمالي الاتفاق", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Text(
                        text = MoneyFormatter.formatEgp(summary.contractor.agreedAmountPiasters),
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("المدفوع حتى الآن", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Text(
                        text = MoneyFormatter.formatEgp(summary.paidPiasters),
                        style = MaterialTheme.typography.titleSmall,
                        color = VolcanicOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (summary.isOverPaid) "زيادة مدفوعة" else "المتبقي له",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                    Text(
                        text = MoneyFormatter.formatEgp(summary.remainingPiasters),
                        style = MaterialTheme.typography.titleSmall,
                        color = when {
                            summary.isOverPaid -> DangerRed
                            summary.isSettled -> EmeraldSafe
                            else -> Slate900
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Slate100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }

            if (summary.isOverPaid) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerRedContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تنبيه: إجمالي الدفعات تجاوز قيمة الاتفاق المتفق عليه!",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (summary.isSettled) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تم سداد إجمالي الاتفاق بالكامل (خالص)",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSafe,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Action Buttons: + Record Payment & Expand Payments History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("record_payment_${summary.contractor.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسجيل دفعة", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Slate300)
                ) {
                    Text(
                        text = "الدفعات (${summary.payments.size})",
                        color = Slate700,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Linked Payments History
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = Slate200)
                    if (summary.payments.isEmpty()) {
                        Text(
                            text = "لم تُسجل دفعات لهذا المقاول بعد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        summary.payments.forEach { paymentRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate50, RoundedCornerShape(8.dp))
                                    .clickable { onEditPayment(paymentRow.expense) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = paymentRow.expense.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${MoneyFormatter.formatDate(paymentRow.expense.dateMillis)} • ${PaymentMethod.fromCode(paymentRow.expense.paymentMethod).labelAr}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = MoneyFormatter.formatEgp(paymentRow.expense.amountPiasters),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = VolcanicOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { onDeletePayment(paymentRow.expense.id) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "حذف الدفعة",
                                            tint = DangerRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
