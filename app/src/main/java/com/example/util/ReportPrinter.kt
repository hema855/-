package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.ui.state.CategoryBudgetSummary
import com.example.ui.state.ContractorSummary
import com.example.ui.state.ExpenseRowItem

object ReportPrinter {
    private var activeWebView: WebView? = null

    fun printFinancialReport(
        context: Context,
        totalBudgetPiasters: Long,
        totalSpentPiasters: Long,
        remainingPiasters: Long,
        completionPercent: Float,
        categories: List<CategoryBudgetSummary>,
        contractors: List<ContractorSummary>,
        expenses: List<ExpenseRowItem>,
        materialsPiasters: Long,
        laborPiasters: Long,
        transportPiasters: Long,
        miscPiasters: Long
    ) {
        val html = buildHtmlReport(
            totalBudgetPiasters = totalBudgetPiasters,
            totalSpentPiasters = totalSpentPiasters,
            remainingPiasters = remainingPiasters,
            completionPercent = completionPercent,
            categories = categories,
            contractors = contractors,
            expenses = expenses,
            materialsPiasters = materialsPiasters,
            laborPiasters = laborPiasters,
            transportPiasters = transportPiasters,
            miscPiasters = miscPiasters
        )

        val webView = WebView(context)
        activeWebView = webView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val jobName = "تقرير_تشطيب_160_${MoneyFormatter.formatFileTimestamp()}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build()
                )
                activeWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun buildHtmlReport(
        totalBudgetPiasters: Long,
        totalSpentPiasters: Long,
        remainingPiasters: Long,
        completionPercent: Float,
        categories: List<CategoryBudgetSummary>,
        contractors: List<ContractorSummary>,
        expenses: List<ExpenseRowItem>,
        materialsPiasters: Long,
        laborPiasters: Long,
        transportPiasters: Long,
        miscPiasters: Long
    ): String {
        val dateStr = MoneyFormatter.formatDate(System.currentTimeMillis())
        val remainClass = if (remainingPiasters < 0) "danger" else "safe"

        val categoryRows = categories.joinToString("\n") { c ->
            val statusClass = if (c.isOverBudget) "danger" else "safe"
            val statusText = if (c.isOverBudget) "تجاوز الميزانية" else "ضمن الميزانية"
            """
            <tr>
                <td><strong>${escapeHtml(c.category.name)}</strong></td>
                <td>${MoneyFormatter.formatEgp(c.category.budgetPiasters)}</td>
                <td>${MoneyFormatter.formatEgp(c.spentPiasters)}</td>
                <td class="$statusClass">${MoneyFormatter.formatEgp(c.remainingPiasters)}</td>
                <td class="$statusClass">${MoneyFormatter.formatPercentage(c.percentUsed)} ($statusText)</td>
            </tr>
            """.trimIndent()
        }

        val contractorRows = if (contractors.isEmpty()) {
            """<tr><td colspan="5" style="text-align:center;color:#64748B;">لا يوجد مقاولون مسجلون حالياً</td></tr>"""
        } else {
            contractors.joinToString("\n") { ct ->
                val remClass = if (ct.remainingPiasters < 0) "danger" else "safe"
                """
                <tr>
                    <td><strong>${escapeHtml(ct.contractor.name)}</strong></td>
                    <td>${escapeHtml(ct.contractor.trade)}</td>
                    <td>${MoneyFormatter.formatEgp(ct.contractor.agreedAmountPiasters)}</td>
                    <td>${MoneyFormatter.formatEgp(ct.paidPiasters)}</td>
                    <td class="$remClass">${MoneyFormatter.formatEgp(ct.remainingPiasters)}</td>
                </tr>
                """.trimIndent()
            }
        }

        val expenseRows = if (expenses.isEmpty()) {
            """<tr><td colspan="6" style="text-align:center;color:#64748B;">لا توجد مصروفات مسجلة حتى الآن</td></tr>"""
        } else {
            expenses.joinToString("\n") { row ->
                val typeLabel = ExpenseType.fromCode(row.expense.expenseType).labelAr
                val payLabel = PaymentMethod.fromCode(row.expense.paymentMethod).labelAr
                """
                <tr>
                    <td>${MoneyFormatter.formatDate(row.expense.dateMillis)}</td>
                    <td>${escapeHtml(row.categoryName)}</td>
                    <td>$typeLabel</td>
                    <td>${escapeHtml(row.expense.description)}</td>
                    <td>$payLabel</td>
                    <td><strong>${MoneyFormatter.formatEgp(row.expense.amountPiasters)}</strong></td>
                </tr>
                """.trimIndent()
            }
        }

        return """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8" />
                <style>
                    body { font-family: sans-serif; color: #0F172A; margin: 20px; font-size: 12px; line-height: 1.5; }
                    h1 { margin: 0 0 4px 0; color: #334155; font-size: 20px; }
                    .subtitle { color: #64748B; margin-bottom: 16px; border-bottom: 2px solid #EA580C; padding-bottom: 8px; }
                    .kpi-grid { display: table; width: 100%; border-collapse: separate; border-spacing: 8px; margin-bottom: 16px; }
                    .kpi-card { display: table-cell; width: 25%; border: 1px solid #CBD5E1; border-radius: 8px; padding: 10px; background: #F8FAFC; }
                    .kpi-label { font-size: 11px; color: #475569; }
                    .kpi-value { font-size: 15px; font-weight: bold; margin-top: 4px; color: #0F172A; }
                    .safe { color: #059669; font-weight: bold; }
                    .danger { color: #DC2626; font-weight: bold; }
                    h2 { font-size: 14px; color: #334155; margin: 18px 0 8px 0; border-right: 4px solid #EA580C; padding-right: 8px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                    th, td { border: 1px solid #CBD5E1; padding: 6px 8px; text-align: right; }
                    th { background: #334155; color: #FFFFFF; font-weight: bold; }
                    tr:nth-child(even) { background: #F8FAFC; }
                    .split-row { margin-bottom: 12px; padding: 8px 12px; background: #F1F5F9; border-radius: 6px; }
                    .split-item { display: inline-block; margin-left: 16px; }
                </style>
            </head>
            <body>
                <h1>تقرير ميزانية ومصروفات تشطيب شقة 160 م²</h1>
                <div class="subtitle">تاريخ التقرير: $dateStr — تطبيق تشطيب 160 (تخزين محلي بدون إنترنت)</div>

                <div class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-label">إجمالي الميزانية المرصودة</div>
                        <div class="kpi-value">${MoneyFormatter.formatEgp(totalBudgetPiasters)}</div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-label">إجمالي المصروف الفعلي</div>
                        <div class="kpi-value">${MoneyFormatter.formatEgp(totalSpentPiasters)}</div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-label">المتبقي من الميزانية</div>
                        <div class="kpi-value $remainClass">${MoneyFormatter.formatEgp(remainingPiasters)}</div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-label">نسبة الإنجاز المالي</div>
                        <div class="kpi-value">${MoneyFormatter.formatPercentage(completionPercent)}</div>
                    </div>
                </div>

                <div class="split-row">
                    <span class="split-item"><strong>خامات:</strong> ${MoneyFormatter.formatEgp(materialsPiasters)}</span>
                    <span class="split-item"><strong>مصنعيات:</strong> ${MoneyFormatter.formatEgp(laborPiasters)}</span>
                    <span class="split-item"><strong>نقل ومشال:</strong> ${MoneyFormatter.formatEgp(transportPiasters)}</span>
                    <span class="split-item"><strong>نثريات:</strong> ${MoneyFormatter.formatEgp(miscPiasters)}</span>
                </div>

                <h2>أولاً: الميزانية التقديرية مقابل المصروف الفعلي للبنود</h2>
                <table>
                    <thead>
                        <tr>
                            <th>البند</th>
                            <th>الميزانية التقديرية</th>
                            <th>المصروف الفعلي</th>
                            <th>المتبقي / العجز</th>
                            <th>النسبة والحالة</th>
                        </tr>
                    </thead>
                    <tbody>$categoryRows</tbody>
                </table>

                <h2>ثانياً: حسابات المقاولين والصنايعية</h2>
                <table>
                    <thead>
                        <tr>
                            <th>الاسم</th>
                            <th>المهنة</th>
                            <th>إجمالي الاتفاق</th>
                            <th>المدفوع</th>
                            <th>المتبقي له</th>
                        </tr>
                    </thead>
                    <tbody>$contractorRows</tbody>
                </table>

                <h2>ثالثاً: سجل المصروفات والفواتير (${expenses.size} حركة)</h2>
                <table>
                    <thead>
                        <tr>
                            <th>التاريخ</th>
                            <th>البند</th>
                            <th>النوع</th>
                            <th>البيان</th>
                            <th>طريقة الدفع</th>
                            <th>المبلغ</th>
                        </tr>
                    </thead>
                    <tbody>$expenseRows</tbody>
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}
