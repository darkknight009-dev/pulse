package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun BudgetGaugeCard(
    monthName: String,
    totalLimit: Double,
    totalSpent: Double,
    onAdjustLimitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalLimit > 0) (totalSpent / totalLimit).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_progress"
    )

    val remaining = maxOf(0.0, totalLimit - totalSpent)
    val percentageUsed = (progress * 100).toInt()
    val isOverBudget = totalSpent > totalLimit
    val daysRemaining = 17
    val safeDailySpend = if (daysRemaining > 0 && remaining > 0) remaining / daysRemaining else 0.0

    val primaryStatusColor = when {
        isOverBudget -> RoseCoral
        percentageUsed > 80 -> WarningAmber
        else -> LiquidCyan
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(24.dp))
            .testTag("budget_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTHLY SPENDING CAP",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = monthName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { onAdjustLimitClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("adjust_limit_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit Limit",
                        tint = GoldChampagne,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Adjust Cap",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = GoldChampagne
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Radial Ring Gauge & Numbers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Arc Gauge Canvas
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        // Track
                        drawArc(
                            color = Color(0xFF1B202D),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    primaryStatusColor,
                                    ElectricTeal,
                                    primaryStatusColor
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentageUsed%",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = primaryStatusColor
                        )
                        Text(
                            text = "UTILIZED",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 7.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Stats Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "₹${"%,.0f".format(totalSpent)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = if (isOverBudget) RoseCoral else TextPrimary
                    )
                    Text(
                        text = "spent of ₹${"%,.0f".format(totalLimit)} monthly allowance",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOverBudget) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Pacing",
                            tint = if (isOverBudget) RoseCoral else EmeraldCash,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOverBudget) "Exceeded by ₹${"%,.0f".format(totalSpent - totalLimit)}" else "₹${"%,.0f".format(remaining)} reserve remaining",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isOverBudget) RoseCoral else EmeraldCash
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Safe Burn Rate Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LiquidCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Safe Burn: ₹${"%,.0f".format(safeDailySpend)}/day",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "$daysRemaining days left",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
