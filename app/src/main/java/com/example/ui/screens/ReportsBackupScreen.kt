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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TableView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.data.model.CategoryIconKey
import com.example.ui.state.TashteebUiState
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
fun ReportsBackupScreen(
    uiState: TashteebUiState,
    onExportJsonBackup: () -> Unit,
    onImportJsonBackup: () -> Unit,
    onExportCsv: () -> Unit,
    onPrintReport: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Offline Data Safety Banner
        item(key = "offline_safety_banner") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(EmeraldContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = EmeraldSafe,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حماية البيانات والتقارير (أوفلاين 100%)",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "جميع بياناتك محفوظة تلقائياً على جهازك. صدّر نسخة احتياطية JSON أسبوعياً لحماية بياناتك من الفقدان.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                }
            }
        }

        // 2. JSON Backup & Restore Card
        item(key = "json_backup_card") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Backup,
                            contentDescription = null,
                            tint = VolcanicOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "النسخ الاحتياطي والاستعادة (JSON Backup)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "يحفظ النسخة الكاملة للبنود (${uiState.categorySummaries.size}) والمصروفات (${uiState.allExpenseRows.size}) والمقاولين (${uiState.contractorSummaries.size}) في ملف JSON واحد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportJsonBackup,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_json_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VolcanicOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير JSON", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onImportJsonBackup,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("import_json_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Slate300)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileUpload,
                                contentDescription = null,
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استيراد JSON", color = Slate700, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. CSV & Clean Printable Report Card
        item(key = "export_print_card") {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TableView,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "تصدير التقارير والطباعة",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "ملف CSV متوافق تماماً مع Excel للغة العربية (UTF-8 BOM)، أو تقرير مطبوع نظيف يمكنك حفظه كـ PDF بضغطة زر.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Button(
                        onClick = onPrintReport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("print_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Print,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("طباعة نظيفة / حفظ تقرير PDF", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_csv_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate300)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TableView,
                            contentDescription = null,
                            tint = EmeraldSafe,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير جدول المصروفات (CSV لإكسل)", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Budget Categories Quick Management Header
        item(key = "manage_budgets_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إدارة الميزانيات التقديرية للبنود",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إجمالي الميزانية: ${MoneyFormatter.formatEgp(uiState.totalBudgetPiasters)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VolcanicOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onAddCategory) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = VolcanicOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة بند", color = VolcanicOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(uiState.categorySummaries, key = { "manage_${it.category.id}" }) { summary ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditCategory(summary.category) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(VolcanicOrangeContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconKey.fromKey(summary.category.iconKey).icon,
                                contentDescription = null,
                                tint = VolcanicOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = summary.category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Slate900,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "الميزانية: ${MoneyFormatter.formatEgp(summary.category.budgetPiasters)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "تعديل الميزانية",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
