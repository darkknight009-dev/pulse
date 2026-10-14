package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentCategory
import com.example.data.model.TransactionItem
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldCash
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun MonthlyLimitsScreen(
    budget: MonthlyBudget,
    transactions: List<TransactionItem>,
    onOpenEditLimitModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMonthTab by remember { mutableStateOf("October 2026") }
    val months = listOf("October 2026", "November 2026", "December 2026")

    val totalSpent = transactions.sumOf { it.amount }
    val totalLimit = budget.totalLimit
    val remaining = maxOf(0.0, totalLimit - totalSpent)
    val pct = if (totalLimit > 0) ((totalSpent / totalLimit) * 100).toInt() else 0

    // Compute category spends
    val diningSpent = transactions.filter { it.category == PaymentCategory.DINING }.sumOf { it.amount }
    val shoppingSpent = transactions.filter { it.category == PaymentCategory.SHOPPING }.sumOf { it.amount }
    val groceriesSpent = transactions.filter { it.category == PaymentCategory.GROCERIES }.sumOf { it.amount }
    val travelSpent = transactions.filter { it.category == PaymentCategory.TRAVEL }.sumOf { it.amount }
    val billsSpent = transactions.filter { it.category == PaymentCategory.BILLS }.sumOf { it.amount }
    val entertainmentSpent = transactions.filter { it.category == PaymentCategory.ENTERTAINMENT }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTHLY BUDGETS",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Custom spending limits & AI pacing per month",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onOpenEditLimitModal,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = LiquidCyan
                    ),
                    modifier = Modifier.testTag("edit_monthly_limit_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit",
                        tint = GoldChampagne,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Set Cap",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // Month Selection Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEach { month ->
                    val isSelected = selectedMonthTab == month
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) DarkSurfaceCard else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) LiquidCyan else DarkBorderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedMonthTab = month }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Main Ceiling Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ALLOWANCE FOR $selectedMonthTab",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "₹${"%,.0f".format(totalLimit)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (pct > 80) RoseCoral.copy(alpha = 0.15f) else EmeraldCash.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$pct% Spent",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (pct > 80) RoseCoral else EmeraldCash
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (totalSpent / totalLimit).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (pct > 85) RoseCoral else LiquidCyan,
                        trackColor = Color(0xFF161B26),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SPENT",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "₹${"%,.0f".format(totalSpent)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RESERVE BUFFER",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "₹${"%,.0f".format(remaining)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EmeraldCash
                            )
                        }
                    }
                }
            }
        }

        // AI Guardrail Callout
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = ElectricTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Automated Spending Pacing Guardrails",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Whenever a payment is made through your linked card, PayPulse AI simulates your remaining month-end buffer and sends instant advisory adjustments.",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Category Limits Header
        item {
            Text(
                text = "CATEGORY ALLOCATION & VELOCITY",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
        }

        item { CategoryPillRow("Dining & Food", diningSpent, budget.diningLimit, PaymentCategory.DINING) }
        item { CategoryPillRow("Groceries & Pantry", groceriesSpent, budget.groceriesLimit, PaymentCategory.GROCERIES) }
        item { CategoryPillRow("Shopping & Tech", shoppingSpent, budget.shoppingLimit, PaymentCategory.SHOPPING) }
        item { CategoryPillRow("Travel & Transit", travelSpent, budget.travelLimit, PaymentCategory.TRAVEL) }
        item { CategoryPillRow("Utilities & Bills", billsSpent, budget.billsLimit, PaymentCategory.BILLS) }
        item { CategoryPillRow("Entertainment", entertainmentSpent, budget.entertainmentLimit, PaymentCategory.ENTERTAINMENT) }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryPillRow(
    categoryName: String,
    spent: Double,
    limit: Double,
    category: PaymentCategory
) {
    val progress = if (limit > 0) (spent / limit).coerceIn(0.0, 1.0).toFloat() else 0f
    val pct = (progress * 100).toInt()
    val isOver = spent > limit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.getIcon(),
                            contentDescription = categoryName,
                            tint = category.color,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoryName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "$pct%",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isOver) RoseCoral else category.color
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOver) RoseCoral else category.color,
                trackColor = Color(0xFF161B26),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${"%,.0f".format(spent)} spent",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Cap: ₹${"%,.0f".format(limit)}",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
