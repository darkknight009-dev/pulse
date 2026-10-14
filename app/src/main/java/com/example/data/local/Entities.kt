package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PaymentCategory
import com.example.data.model.TransactionItem

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val merchantName: String,
    val category: String,
    val amount: Double,
    val timestamp: Long,
    val paytmRefId: String,
    val note: String,
    val aiAdvice: String
) {
    fun toModel(): TransactionItem = TransactionItem(
        id = id,
        title = title,
        merchantName = merchantName,
        category = try {
            PaymentCategory.valueOf(category)
        } catch (_: Exception) {
            PaymentCategory.OTHER
        },
        amount = amount,
        timestamp = timestamp,
        paytmRefId = paytmRefId,
        note = note,
        aiAdvice = aiAdvice
    )

    companion object {
        fun fromModel(model: TransactionItem): TransactionEntity = TransactionEntity(
            id = model.id,
            title = model.title,
            merchantName = model.merchantName,
            category = model.category.name,
            amount = model.amount,
            timestamp = model.timestamp,
            paytmRefId = model.paytmRefId,
            note = model.note,
            aiAdvice = model.aiAdvice
        )
    }
}

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
    @PrimaryKey val monthKey: String, // e.g., "2026-10"
    val displayMonth: String,
    val totalLimit: Double,
    val diningLimit: Double,
    val shoppingLimit: Double,
    val groceriesLimit: Double,
    val travelLimit: Double,
    val billsLimit: Double,
    val entertainmentLimit: Double,
    val healthLimit: Double
)
