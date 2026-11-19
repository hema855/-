package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val budgetPiasters: Long,
    val iconKey: String = "CUSTOM",
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "contractors",
    indices = [Index(value = ["categoryId"])]
)
data class ContractorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val trade: String,
    val categoryId: Long,
    val agreedAmountPiasters: Long,
    val phone: String = "",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ContractorEntity::class,
            parentColumns = ["id"],
            childColumns = ["contractorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["contractorId"]),
        Index(value = ["dateMillis"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateMillis: Long,
    val categoryId: Long,
    val expenseType: String,
    val description: String,
    val quantity: Double? = null,
    val unitPricePiasters: Long? = null,
    val amountPiasters: Long,
    val paymentMethod: String,
    val contractorId: Long? = null,
    val countsTowardAgreement: Boolean = true,
    val receiptImagePath: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
