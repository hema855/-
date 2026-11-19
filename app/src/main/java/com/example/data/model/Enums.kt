package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Carpenter
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Foundation
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Roofing
import androidx.compose.material.icons.rounded.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.LaborOrange
import com.example.ui.theme.LaborOrangeContainer
import com.example.ui.theme.MaterialsBlue
import com.example.ui.theme.MaterialsBlueContainer
import com.example.ui.theme.MiscSlate
import com.example.ui.theme.MiscSlateContainer
import com.example.ui.theme.TransportTeal
import com.example.ui.theme.TransportTealContainer

enum class ExpenseType(
    val code: String,
    val labelAr: String,
    val icon: ImageVector,
    val accentColor: Color,
    val containerColor: Color
) {
    MATERIALS(
        code = "MATERIALS",
        labelAr = "خامات",
        icon = Icons.Rounded.Inventory2,
        accentColor = MaterialsBlue,
        containerColor = MaterialsBlueContainer
    ),
    LABOR(
        code = "LABOR",
        labelAr = "مصنعية",
        icon = Icons.Rounded.Engineering,
        accentColor = LaborOrange,
        containerColor = LaborOrangeContainer
    ),
    TRANSPORT(
        code = "TRANSPORT",
        labelAr = "نقل ومشال",
        icon = Icons.Rounded.LocalShipping,
        accentColor = TransportTeal,
        containerColor = TransportTealContainer
    ),
    MISC(
        code = "MISC",
        labelAr = "نثريات",
        icon = Icons.Rounded.MoreHoriz,
        accentColor = MiscSlate,
        containerColor = MiscSlateContainer
    );

    companion object {
        fun fromCode(code: String): ExpenseType =
            entries.firstOrNull { it.code == code } ?: MATERIALS
    }
}

enum class PaymentMethod(
    val code: String,
    val labelAr: String,
    val icon: ImageVector
) {
    CASH(
        code = "CASH",
        labelAr = "كاش",
        icon = Icons.Rounded.Payments
    ),
    INSTAPAY(
        code = "INSTAPAY",
        labelAr = "إنستاباي",
        icon = Icons.Rounded.PhoneAndroid
    ),
    EWALLET(
        code = "EWALLET",
        labelAr = "محفظة إلكترونية",
        icon = Icons.Rounded.AccountBalanceWallet
    );

    companion object {
        fun fromCode(code: String): PaymentMethod =
            entries.firstOrNull { it.code == code } ?: CASH
    }
}

enum class CategoryIconKey(
    val key: String,
    val labelAr: String,
    val icon: ImageVector
) {
    MASONRY("MASONRY", "مباني", Icons.Rounded.Foundation),
    ELECTRICAL("ELECTRICAL", "كهرباء", Icons.Rounded.Bolt),
    PLUMBING("PLUMBING", "سباكة", Icons.Rounded.Plumbing),
    CERAMIC("CERAMIC", "سيراميك", Icons.Rounded.GridView),
    GYPSUM("GYPSUM", "جبس", Icons.Rounded.Roofing),
    PAINT("PAINT", "دهانات", Icons.Rounded.FormatPaint),
    CARPENTRY("CARPENTRY", "نجارة", Icons.Rounded.DoorFront),
    ALUMINUM("ALUMINUM", "ألوميتال", Icons.Rounded.Window),
    TOOLS("TOOLS", "أعمال", Icons.Rounded.Build),
    WOOD("WOOD", "أخشاب", Icons.Rounded.Carpenter),
    CUSTOM("CUSTOM", "عام", Icons.Rounded.Category);

    companion object {
        fun fromKey(key: String): CategoryIconKey =
            entries.firstOrNull { it.key == key } ?: CUSTOM
    }
}
