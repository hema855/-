package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.data.model.CategoryIconKey
import com.example.ui.state.CategoryBudgetSummary
import com.example.ui.state.TashteebUiState
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBorder
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.LaborOrange
import com.example.ui.theme.MaterialsBlue
import com.example.ui.theme.MiscSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TransportTeal
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.util.MoneyFormatter

@Composable
fun DashboardScreen(
    uiState: TashteebUiState,
    onAddCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onOpenCategoryExpenses: (Long) -> Unit,
    onQuickAddForCategory: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Site Command Header Banner
        item(key = "hero_banner") {
            SiteHeroBanner(
                uiState = uiState,
                onAddCategory = onAddCategory
            )
        }

        // 2. Four High-Contrast KPI Summary Cards
        item(key = "kpi_cards") {
            KpiSummaryGrid(uiState = uiState)
        }

        // 3. Materials vs Labor Breakdown Card
        item(key = "split_card") {
            MaterialsVsLaborCard(uiState = uiState)
        }

        // 4. Category Budget vs Actual Section Header
        item(key = "categories_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "بنود التشطيب (الميزانية مقابل الفعلي)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.categorySummaries.size} بنود • اضغط على أي بند لعرض فواتيره",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
                TextButton(
                    onClick = onAddCategory,
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = VolcanicOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بند جديد",
                        color = VolcanicOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5. Category Budget Progress Cards
        items(uiState.categorySummaries, key = { it.category.id }) { summary ->
            CategoryProgressCard(
                summary = summary,
                onCardClick = { onOpenCategoryExpenses(summary.category.id) },
                onEditClick = { onEditCategory(summary.category) },
                onQuickAddClick = { onQuickAddForCategory(summary.category.id) }
            )
        }
    }
}

@Composable
private fun SiteHeroBanner(
    uiState: TashteebUiState,
    onAddCategory: () -> Unit
) {
    val isTotalOver = uiState.remainingPiasters < 0L
    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Slate800, Slate700)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(VolcanicOrange, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HomeWork,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "تشطيب شقة 160 م²",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "لوحة متابعة الميزانية • أوفلاين بالكامل",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTotalOver) DangerRed else EmeraldSafe
                    ) {
                        Text(
                            text = if (isTotalOver) "تجاوز الميزانية" else "الميزانية آمنة",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Alert strip if any category exceeded 100%
                if (uiState.overBudgetCategoriesCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRedContainer,
                        border = BorderStroke(1.dp, DangerRedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تنبيه: يوجد ${uiState.overBudgetCategoriesCount} بنود تجاوزت الميزانية التقديرية!",
                                style = MaterialTheme.typography.labelMedium,
                                color = DangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiSummaryGrid(uiState: TashteebUiState) {
    val isRemainingNegative = uiState.remainingPiasters < 0L
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 600.dp
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "إجمالي الميزانية المرصودة",
                    value = MoneyFormatter.formatEgp(uiState.totalBudgetPiasters),
                    subtitle = "${uiState.categorySummaries.size} بنود تقديرية",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    accentColor = Slate700,
                    badgeBg = Slate100,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "إجمالي المصروف الفعلي",
                    value = MoneyFormatter.formatEgp(uiState.totalSpentPiasters),
                    subtitle = "${uiState.allExpenseRows.size} فاتورة / حركة",
                    icon = Icons.Rounded.ReceiptLong,
                    accentColor = if (isRemainingNegative) DangerRed else VolcanicOrange,
                    badgeBg = if (isRemainingNegative) DangerRedContainer else VolcanicOrangeContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = if (isRemainingNegative) "العجز عن الميزانية" else "المتبقي من الميزانية",
                    value = MoneyFormatter.formatEgp(uiState.remainingPiasters),
                    subtitle = if (isRemainingNegative) "تجاوزت السقف المقدر" else "رصيد متاح للصرف",
                    icon = Icons.Rounded.Savings,
                    accentColor = if (isRemainingNegative) DangerRed else EmeraldSafe,
                    badgeBg = if (isRemainingNegative) DangerRedContainer else EmeraldContainer,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "نسبة الإنجاز المالي",
                    value = MoneyFormatter.formatPercentage(uiState.completionPercent),
                    subtitle = "من إجمالي الميزانية",
                    icon = Icons.Rounded.PieChart,
                    accentColor = if (uiState.completionPercent > 100f) DangerRed else Slate700,
                    badgeBg = if (uiState.completionPercent > 100f) DangerRedContainer else Slate100,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "إجمالي الميزانية المرصودة",
                        value = MoneyFormatter.formatEgp(uiState.totalBudgetPiasters),
                        subtitle = "${uiState.categorySummaries.size} بنود تشطيب",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        accentColor = Slate700,
                        badgeBg = Slate100,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "إجمالي المصروف الفعلي",
                        value = MoneyFormatter.formatEgp(uiState.totalSpentPiasters),
                        subtitle = "${uiState.allExpenseRows.size} حركة مسجلة",
                        icon = Icons.Rounded.ReceiptLong,
                        accentColor = if (isRemainingNegative) DangerRed else VolcanicOrange,
                        badgeBg = if (isRemainingNegative) DangerRedContainer else VolcanicOrangeContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = if (isRemainingNegative) "العجز عن الميزانية" else "المتبقي من الميزانية",
                        value = MoneyFormatter.formatEgp(uiState.remainingPiasters),
                        subtitle = if (isRemainingNegative) "تجاوزت السقف المقدر" else "ضمن النطاق الآمن",
                        icon = Icons.Rounded.Savings,
                        accentColor = if (isRemainingNegative) DangerRed else EmeraldSafe,
                        badgeBg = if (isRemainingNegative) DangerRedContainer else EmeraldContainer,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "نسبة الإنجاز المالي",
                        value = MoneyFormatter.formatPercentage(uiState.completionPercent),
                        subtitle = "المصروف ÷ الميزانية",
                        icon = Icons.Rounded.PieChart,
                        accentColor = if (uiState.completionPercent > 100f) DangerRed else Slate700,
                        badgeBg = if (uiState.completionPercent > 100f) DangerRedContainer else Slate100,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(badgeBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Slate600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MaterialsVsLaborCard(uiState: TashteebUiState) {
    val total = uiState.totalSpentPiasters
    val matPct = MoneyFormatter.computePercentage(uiState.materialsPiasters, total)
    val labPct = MoneyFormatter.computePercentage(uiState.laborPiasters, total)
    val transPct = MoneyFormatter.computePercentage(uiState.transportPiasters, total)
    val miscPct = MoneyFormatter.computePercentage(uiState.miscPiasters, total)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "توزيع المصروفات (خامات مقابل مصنعيات)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate900,
                    fontWeight = FontWeight.Bold
                )
                if (total == 0L) {
                    Text(
                        text = "بانتظار أول مصروف",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // Stacked horizontal visual comparison bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Slate100)
            ) {
                if (total > 0L) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (uiState.materialsPiasters > 0L) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.materialsPiasters.toFloat())
                                    .fillMaxHeight()
                                    .background(MaterialsBlue)
                            )
                        }
                        if (uiState.laborPiasters > 0L) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.laborPiasters.toFloat())
                                    .fillMaxHeight()
                                    .background(LaborOrange)
                            )
                        }
                        if (uiState.transportPiasters > 0L) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.transportPiasters.toFloat())
                                    .fillMaxHeight()
                                    .background(TransportTeal)
                            )
                        }
                        if (uiState.miscPiasters > 0L) {
                            Box(
                                modifier = Modifier
                                    .weight(uiState.miscPiasters.toFloat())
                                    .fillMaxHeight()
                                    .background(MiscSlate)
                            )
                        }
                    }
                }
            }

            // 2x2 Breakdown Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplitLegendPill(
                    label = "خامات",
                    amount = MoneyFormatter.formatEgp(uiState.materialsPiasters),
                    percent = MoneyFormatter.formatPercentage(matPct),
                    color = MaterialsBlue,
                    icon = Icons.Rounded.Inventory2,
                    modifier = Modifier.weight(1f)
                )
                SplitLegendPill(
                    label = "مصنعيات",
                    amount = MoneyFormatter.formatEgp(uiState.laborPiasters),
                    percent = MoneyFormatter.formatPercentage(labPct),
                    color = LaborOrange,
                    icon = Icons.Rounded.Engineering,
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.transportPiasters > 0L || uiState.miscPiasters > 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SplitLegendPill(
                        label = "نقل ومشال",
                        amount = MoneyFormatter.formatEgp(uiState.transportPiasters),
                        percent = MoneyFormatter.formatPercentage(transPct),
                        color = TransportTeal,
                        icon = Icons.Rounded.LocalShipping,
                        modifier = Modifier.weight(1f)
                    )
                    SplitLegendPill(
                        label = "نثريات",
                        amount = MoneyFormatter.formatEgp(uiState.miscPiasters),
                        percent = MoneyFormatter.formatPercentage(miscPct),
                        color = MiscSlate,
                        icon = Icons.Rounded.MoreHoriz,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitLegendPill(
    label: String,
    amount: String,
    percent: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Slate50,
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate700,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = percent,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate900,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryProgressCard(
    summary: CategoryBudgetSummary,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onQuickAddClick: () -> Unit
) {
    val barColor = when {
        summary.isOverBudget -> DangerRed
        summary.isWarning -> AmberWarning
        else -> EmeraldSafe
    }
    val cardBorderColor = when {
        summary.isOverBudget -> DangerRedBorder
        summary.isWarning -> AmberBorder
        else -> Slate200
    }
    val badgeBg = when {
        summary.isOverBudget -> DangerRedContainer
        summary.isWarning -> AmberContainer
        else -> EmeraldContainer
    }
    val targetFraction = (summary.percentUsed / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = 300f),
        label = "category_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("category_card_${summary.category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(if (summary.isOverBudget) 1.5.dp else 1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (summary.isOverBudget) DangerRedContainer else Slate100,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIconKey.fromKey(summary.category.iconKey).icon,
                            contentDescription = null,
                            tint = if (summary.isOverBudget) DangerRed else Slate700,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.category.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                        if (summary.category.description.isNotBlank()) {
                            Text(
                                text = summary.category.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQuickAddClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("category_quick_add_${summary.category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "إضافة مصروف للبند",
                            tint = VolcanicOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("category_edit_${summary.category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "تعديل ميزانية البند",
                            tint = Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Slate100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(barColor)
                )
            }

            // Financial Numbers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المصروف الفعلي",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                    Text(
                        text = MoneyFormatter.formatEgp(summary.spentPiasters),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (summary.isOverBudget) DangerRed else Slate900,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الميزانية التقديرية",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                    Text(
                        text = MoneyFormatter.formatEgp(summary.category.budgetPiasters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    border = BorderStroke(
                        1.dp,
                        when {
                            summary.isOverBudget -> DangerRedBorder
                            summary.isWarning -> AmberBorder
                            else -> EmeraldBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (summary.isOverBudget) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = barColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (summary.isOverBudget) {
                                "تجاوز ${MoneyFormatter.formatPercentage(summary.percentUsed)}"
                            } else {
                                MoneyFormatter.formatPercentage(summary.percentUsed)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = barColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Over-budget explicit alert footer
            if (summary.isOverBudget) {
                val excess = summary.spentPiasters - summary.category.budgetPiasters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DangerRedContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تحذير: تجاوز البند ميزانيته التقديرية",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ ${MoneyFormatter.formatEgp(excess)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
