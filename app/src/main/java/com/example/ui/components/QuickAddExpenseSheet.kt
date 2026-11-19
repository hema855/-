package com.example.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.CategoryEntity
import com.example.data.local.ContractorEntity
import com.example.data.local.ExpenseEntity
import com.example.data.model.CategoryIconKey
import com.example.data.model.ExpenseType
import com.example.data.model.PaymentMethod
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VolcanicOrange
import com.example.ui.theme.VolcanicOrangeContainer
import com.example.util.MoneyFormatter
import com.example.util.ReceiptImageStorage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddExpenseSheet(
    categories: List<CategoryEntity>,
    contractors: List<ContractorEntity>,
    initialExpense: ExpenseEntity? = null,
    preselectedCategoryId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultCatId = initialExpense?.categoryId
        ?: preselectedCategoryId
        ?: categories.firstOrNull()?.id
        ?: 1L

    var selectedCategoryId by remember { mutableLongStateOf(defaultCatId) }
    var selectedType by remember {
        mutableStateOf(
            initialExpense?.let { ExpenseType.fromCode(it.expenseType) } ?: ExpenseType.MATERIALS
        )
    }
    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var useQtyCalculator by remember {
        mutableStateOf(initialExpense?.quantity != null && initialExpense.unitPricePiasters != null)
    }
    var directAmountText by remember {
        mutableStateOf(initialExpense?.let { MoneyFormatter.piastersToEditableString(it.amountPiasters) } ?: "")
    }
    var quantityText by remember {
        mutableStateOf(initialExpense?.quantity?.let { MoneyFormatter.formatQuantity(it).replace(",", "") } ?: "")
    }
    var unitPriceText by remember {
        mutableStateOf(initialExpense?.unitPricePiasters?.let { MoneyFormatter.piastersToEditableString(it) } ?: "")
    }
    var selectedPayment by remember {
        mutableStateOf(
            initialExpense?.let { PaymentMethod.fromCode(it.paymentMethod) } ?: PaymentMethod.CASH
        )
    }
    var dateMillis by remember {
        mutableLongStateOf(initialExpense?.dateMillis ?: MoneyFormatter.todayStartMillis())
    }
    var selectedContractorId by remember { mutableStateOf(initialExpense?.contractorId) }
    var countsTowardAgreement by remember {
        mutableStateOf(initialExpense?.countsTowardAgreement ?: true)
    }
    var contractorMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var receiptImagePath by remember { mutableStateOf(initialExpense?.receiptImagePath) }
    var isProcessingImage by remember { mutableStateOf(false) }
    var showReceiptPreview by remember { mutableStateOf(false) }
    var pendingCameraTempFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sessionCreatedReceipts = remember { mutableStateListOf<String>() }

    fun importReceiptUri(uri: Uri, tempFileToDelete: File? = null) {
        scope.launch {
            isProcessingImage = true
            validationError = null
            val savedName = withContext(Dispatchers.IO) {
                ReceiptImageStorage.saveReceiptFromUri(context, uri).also {
                    tempFileToDelete?.delete()
                }
            }
            isProcessingImage = false
            if (savedName != null) {
                sessionCreatedReceipts.add(savedName)
                receiptImagePath = savedName
            } else {
                validationError = "تعذر معالجة صورة الفاتورة. جرّب صورة أخرى."
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            importReceiptUri(uri)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        val tempFile = pendingCameraTempFile
        pendingCameraUri = null
        pendingCameraTempFile = null
        if (success && uri != null) {
            importReceiptUri(uri, tempFile)
        } else {
            tempFile?.delete()
        }
    }

    val handleDismiss: () -> Unit = {
        // Delete any new receipt images captured during this cancelled session
        sessionCreatedReceipts.forEach { fileName ->
            if (fileName != initialExpense?.receiptImagePath) {
                ReceiptImageStorage.deleteReceiptFile(context, fileName)
            }
        }
        onDismiss()
    }

    val computedPiasters: Long? = if (useQtyCalculator) {
        MoneyFormatter.computeLineTotalPiasters(quantityText, unitPriceText)
    } else {
        MoneyFormatter.parseEgpToPiasters(directAmountText)
    }

    val quickSuggestions = remember(selectedCategoryId, categories) {
        val catName = categories.firstOrNull { it.id == selectedCategoryId }?.name.orEmpty()
        when {
            catName.contains("كهرباء") -> listOf("أسلاك السويدي", "لوحة قواطع", "سبوتات وليد", "دفعة كهربائي")
            catName.contains("سباكة") -> listOf("مواسير وقطع تغذية", "خامات عزل", "أطقم صحية", "دفعة سباك")
            catName.contains("سيراميك") -> listOf("سيراميك وبورسلين", "أسمنت ورمل", "مادة لصق وسقية", "مشال وتشوين")
            catName.contains("جبس") -> listOf("ألواح جبس وصاج", "شفاط سقف مركزي", "معجون جبس", "دفعة صنايعي جبس")
            catName.contains("دهان") -> listOf("شيكارة معجون", "سيلر وبطانة", "بلاستيك ألوان", "دفعة نقاش")
            catName.contains("نجار") -> listOf("أبواب WPC حمامات", "باب جرار درسينج", "وحدة الماستر", "كوالين ومفصلات")
            catName.contains("ألوميتال") -> listOf("دفعة ألوميتال / UPVC", "زجاج دبل", "سلك بليسيه")
            else -> listOf("طوب وأسمنت", "رمل وزلط", "مشال ونقل", "مصنعية يومية")
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // High-contrast top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate700)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (initialExpense == null) "إضافة مصروف سريع" else "تعديل المصروف",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Text(
                        text = "تسجيل فوري ودقيق بالجنيه المصري (ج.م)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )
                }
                IconButton(
                    onClick = handleDismiss,
                    modifier = Modifier.testTag("close_quick_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "إغلاق",
                        tint = PureWhite
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Amount Section (Direct Total vs Qty × Unit Price)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المبلغ (ج.م)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Slate900
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (useQtyCalculator) VolcanicOrangeContainer else PureWhite,
                                border = BorderStroke(1.dp, if (useQtyCalculator) VolcanicOrange else Slate300),
                                modifier = Modifier
                                    .clickable {
                                        useQtyCalculator = !useQtyCalculator
                                        validationError = null
                                    }
                                    .testTag("toggle_qty_calculator")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Calculate,
                                        contentDescription = null,
                                        tint = if (useQtyCalculator) VolcanicOrange else Slate700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (useQtyCalculator) "إدخال الإجمالي مباشرة" else "حساب: كمية × سعر",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (useQtyCalculator) VolcanicOrange else Slate700
                                    )
                                }
                            }
                        }

                        if (!useQtyCalculator) {
                            OutlinedTextField(
                                value = directAmountText,
                                onValueChange = {
                                    directAmountText = it
                                    validationError = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expense_amount_input"),
                                placeholder = { Text("مثال: 4500") },
                                suffix = {
                                    Text(
                                        text = "ج.م",
                                        fontWeight = FontWeight.Bold,
                                        color = VolcanicOrange
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PureWhite,
                                    unfocusedContainerColor = PureWhite,
                                    focusedBorderColor = VolcanicOrange,
                                    unfocusedBorderColor = Slate300
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = {
                                        quantityText = it
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("expense_quantity_input"),
                                    label = { Text("الكمية") },
                                    placeholder = { Text("10") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = PureWhite,
                                        unfocusedContainerColor = PureWhite,
                                        focusedBorderColor = VolcanicOrange
                                    )
                                )
                                OutlinedTextField(
                                    value = unitPriceText,
                                    onValueChange = {
                                        unitPriceText = it
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("expense_unit_price_input"),
                                    label = { Text("سعر الوحدة (ج.م)") },
                                    placeholder = { Text("450") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = PureWhite,
                                        unfocusedContainerColor = PureWhite,
                                        focusedBorderColor = VolcanicOrange
                                    )
                                )
                            }
                            if (computedPiasters != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PureWhite, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الإجمالي المحسوب بدقة:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate600
                                    )
                                    Text(
                                        text = MoneyFormatter.formatEgp(computedPiasters),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EmeraldSafe,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Category Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "البند الرئيسي",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            val selected = cat.id == selectedCategoryId
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCategoryId = cat.id },
                                label = {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = CategoryIconKey.fromKey(cat.iconKey).icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate700,
                                    selectedLabelColor = PureWhite,
                                    selectedLeadingIconColor = VolcanicOrange,
                                    containerColor = Slate100,
                                    labelColor = Slate700
                                ),
                                modifier = Modifier.testTag("category_chip_${cat.id}")
                            )
                        }
                    }
                }

                // 3. Expense Type Selector (4 types)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "نوع المصروف",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExpenseType.entries.forEach { type ->
                            val selected = selectedType == type
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) type.accentColor else Slate100,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selected) type.accentColor else Slate300
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                                    .testTag("expense_type_${type.code}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = type.icon,
                                        contentDescription = null,
                                        tint = if (selected) PureWhite else type.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = type.labelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) PureWhite else Slate900,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Description + Smart Suggestions
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            validationError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_description_input"),
                        label = { Text("البيان / الوصف") },
                        placeholder = { Text("مثال: 10 لفات سلك السويدي 1.5مم") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VolcanicOrange,
                            unfocusedBorderColor = Slate300
                        )
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickSuggestions) { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.clickable {
                                    description = if (description.isBlank()) suggestion else "$description - $suggestion"
                                }
                            ) {
                                Text(
                                    text = "+ $suggestion",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Payment Method
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "طريقة الدفع",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            val selected = selectedPayment == method
                            FilterChip(
                                selected = selected,
                                onClick = { selectedPayment = method },
                                label = {
                                    Text(
                                        text = method.labelAr,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (selected) Icons.Rounded.Check else method.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VolcanicOrange,
                                    selectedLabelColor = PureWhite,
                                    selectedLeadingIconColor = PureWhite,
                                    containerColor = Slate100,
                                    labelColor = Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("payment_method_${method.code}")
                            )
                        }
                    }
                }

                // 6. Date Quick Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val todayMillis = remember { MoneyFormatter.todayStartMillis() }
                    val yesterdayMillis = remember { MoneyFormatter.yesterdayStartMillis() }

                    FilterChip(
                        selected = MoneyFormatter.formatIsoDate(dateMillis) == MoneyFormatter.formatIsoDate(todayMillis),
                        onClick = { dateMillis = todayMillis },
                        label = { Text("اليوم") }
                    )
                    FilterChip(
                        selected = MoneyFormatter.formatIsoDate(dateMillis) == MoneyFormatter.formatIsoDate(yesterdayMillis),
                        onClick = { dateMillis = yesterdayMillis },
                        label = { Text("أمس") }
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        border = BorderStroke(1.dp, Slate300),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true }
                            .testTag("expense_date_picker_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "اختيار التاريخ",
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

                // 7. Optional Contractor Link
                if (contractors.isNotEmpty()) {
                    val selectedContractor = contractors.firstOrNull { it.id == selectedContractorId }
                    ExposedDropdownMenuBox(
                        expanded = contractorMenuExpanded,
                        onExpandedChange = { contractorMenuExpanded = !contractorMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedContractor?.let { "${it.name} (${it.trade})" } ?: "بدون ربط بمقاول (اختياري)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ربط بمقاول / صنايعي") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = Slate600
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = contractorMenuExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VolcanicOrange,
                                unfocusedBorderColor = Slate300
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = contractorMenuExpanded,
                            onDismissRequest = { contractorMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("بدون ربط بمقاول") },
                                onClick = {
                                    selectedContractorId = null
                                    contractorMenuExpanded = false
                                }
                            )
                            contractors.forEach { contractor ->
                                DropdownMenuItem(
                                    text = { Text("${contractor.name} — ${contractor.trade}") },
                                    onClick = {
                                        selectedContractorId = contractor.id
                                        if (selectedType == ExpenseType.LABOR) {
                                            countsTowardAgreement = true
                                        }
                                        contractorMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedContractorId != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { countsTowardAgreement = !countsTowardAgreement }
                        ) {
                            Checkbox(
                                checked = countsTowardAgreement,
                                onCheckedChange = { countsTowardAgreement = it }
                            )
                            Text(
                                text = "خصم هذا المبلغ من إجمالي اتفاق المقاول",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate700
                            )
                        }
                    }
                }

                // 8. Receipt / Invoice Photo Attachment (Camera or Photo Picker - Zero Storage Permissions)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Receipt,
                                contentDescription = null,
                                tint = VolcanicOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "صورة الفاتورة (اختياري)",
                                style = MaterialTheme.typography.titleSmall,
                                color = Slate900
                            )
                        }

                        if (isProcessingImage) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = VolcanicOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "جاري حفظ وضغط صورة الفاتورة...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        } else if (receiptImagePath != null) {
                            val previewFile = remember(receiptImagePath) {
                                ReceiptImageStorage.resolveReceiptFile(context, receiptImagePath)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PureWhite, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AsyncImage(
                                    model = previewFile,
                                    contentDescription = "معاينة صورة الفاتورة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate200)
                                        .clickable { showReceiptPreview = true }
                                        .testTag("receipt_preview_thumbnail")
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "تم إرفاق صورة الفاتورة",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmeraldSafe,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "اضغط على الصورة للتكبير والمعاينة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                                IconButton(
                                    onClick = { receiptImagePath = null },
                                    modifier = Modifier.testTag("remove_receipt_photo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "حذف صورة الفاتورة",
                                        tint = DangerRed
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val (file, uri) = ReceiptImageStorage.createTempCameraUri(context)
                                        pendingCameraTempFile = file
                                        pendingCameraUri = uri
                                        takePictureLauncher.launch(uri)
                                    } catch (_: ActivityNotFoundException) {
                                        validationError = "لا يوجد تطبيق كاميرا متاح على هذا الجهاز. استخدم الاختيار من المعرض."
                                    } catch (_: Exception) {
                                        validationError = "تعذر فتح الكاميرا. جرّب اختيار الصورة من المعرض."
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("capture_receipt_camera_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Slate300),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = PureWhite,
                                    contentColor = Slate900
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = VolcanicOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (receiptImagePath == null) "تصوير فاتورة" else "تصوير جديدة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("pick_receipt_gallery_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Slate300),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = PureWhite,
                                    contentColor = Slate900
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Slate700,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (receiptImagePath == null) "من المعرض" else "تغيير الصورة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            HorizontalDivider(color = Slate200)

            // Primary Volcanic Orange Save CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val finalPiasters = computedPiasters
                        if (finalPiasters == null || finalPiasters <= 0L) {
                            validationError = "يرجى إدخال مبلغ صحيح أكبر من صفر."
                            return@Button
                        }
                        val cleanDesc = description.trim().ifEmpty {
                            val catName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "تشطيب"
                            "${selectedType.labelAr} — $catName"
                        }
                        val qtyVal = if (useQtyCalculator) MoneyFormatter.parsePositiveDouble(quantityText) else null
                        val unitVal = if (useQtyCalculator) MoneyFormatter.parseEgpToPiasters(unitPriceText) else null

                        // Delete unused temporary receipts created earlier in this session
                        sessionCreatedReceipts.forEach { fileName ->
                            if (fileName != receiptImagePath && fileName != initialExpense?.receiptImagePath) {
                                ReceiptImageStorage.deleteReceiptFile(context, fileName)
                            }
                        }

                        onSave(
                            ExpenseEntity(
                                id = initialExpense?.id ?: 0L,
                                dateMillis = dateMillis,
                                categoryId = selectedCategoryId,
                                expenseType = selectedType.code,
                                description = cleanDesc,
                                quantity = qtyVal,
                                unitPricePiasters = unitVal,
                                amountPiasters = finalPiasters,
                                paymentMethod = selectedPayment.code,
                                contractorId = selectedContractorId,
                                countsTowardAgreement = if (selectedContractorId != null) countsTowardAgreement else false,
                                receiptImagePath = receiptImagePath,
                                createdAtMillis = initialExpense?.createdAtMillis ?: System.currentTimeMillis()
                            )
                        )
                    },
                    enabled = !isProcessingImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VolcanicOrange,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (computedPiasters != null && computedPiasters > 0L) {
                            "حفظ المصروف (${MoneyFormatter.formatEgp(computedPiasters)})"
                        } else {
                            "حفظ المصروف"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showReceiptPreview && receiptImagePath != null) {
        ReceiptViewerDialog(
            receiptFileName = receiptImagePath!!,
            title = description.ifBlank { "معاينة صورة الفاتورة" },
            amountText = computedPiasters?.let { MoneyFormatter.formatEgp(it) },
            onDismiss = { showReceiptPreview = false }
        )
    }

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
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("إلغاء")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
