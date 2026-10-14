package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonTeal
import com.example.ui.theme.NeonViolet

enum class PaymentCategory(
    val displayName: String,
    val color: Color
) {
    DINING("Dining & Food", NeonCoral),
    SHOPPING("Shopping", NeonPurple),
    GROCERIES("Groceries", NeonGreen),
    TRAVEL("Travel & Commute", NeonCyan),
    BILLS("Utilities & Bills", NeonAmber),
    ENTERTAINMENT("Entertainment", NeonViolet),
    HEALTH("Health & Wellness", NeonTeal),
    OTHER("Miscellaneous", Color(0xFF64748B));

    fun getIcon(): ImageVector = when (this) {
        DINING -> Icons.Default.Fastfood
        SHOPPING -> Icons.Default.ShoppingBag
        GROCERIES -> Icons.Default.LocalMall
        TRAVEL -> Icons.Default.Flight
        BILLS -> Icons.Default.Lightbulb
        ENTERTAINMENT -> Icons.Default.Movie
        HEALTH -> Icons.Default.Spa
        OTHER -> Icons.Default.Receipt
    }
}

data class TransactionItem(
    val id: Long = 0,
    val title: String,
    val merchantName: String,
    val category: PaymentCategory,
    val amount: Double,
    val timestamp: Long,
    val paytmRefId: String,
    val note: String = "",
    val aiAdvice: String = ""
)

data class MonthlyBudget(
    val monthKey: String, // e.g. "2026-10"
    val displayMonth: String, // e.g. "October 2026"
    val totalLimit: Double,
    val diningLimit: Double,
    val shoppingLimit: Double,
    val groceriesLimit: Double,
    val travelLimit: Double,
    val billsLimit: Double,
    val entertainmentLimit: Double,
    val healthLimit: Double
)

data class SavingRecommendation(
    val id: String,
    val title: String,
    val potentialSavings: Double,
    val insight: String,
    val actionText: String,
    val category: PaymentCategory
)
