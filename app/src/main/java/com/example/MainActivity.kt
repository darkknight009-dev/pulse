package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LimitSettingsModal
import com.example.ui.components.LinkPaytmModal
import com.example.ui.components.QuickPayModal
import com.example.ui.screens.AICoachScreen
import com.example.ui.screens.CardWalletScreen
import com.example.ui.screens.MonthlyLimitsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PayPulseTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PayPulseViewModel
import com.example.ui.viewmodel.ScreenTab
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayPulseTheme {
                PayPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayPulseApp(viewModel: PayPulseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currentBudget by viewModel.currentBudget.collectAsState()
    val paytmAccount by viewModel.paytmAccount.collectAsState()

    var showQuickPayModal by remember { mutableStateOf(false) }
    var showLimitModal by remember { mutableStateOf(false) }
    var showLinkPaytmModal by remember { mutableStateOf(false) }

    val paySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val limitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val linkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = uiState.currentTab != ScreenTab.CARD_WALLET) {
        viewModel.setTab(ScreenTab.CARD_WALLET)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                // Tab 1: Card & Wallet
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.CARD_WALLET,
                    onClick = { viewModel.setTab(ScreenTab.CARD_WALLET) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.CARD_WALLET) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                            contentDescription = "Card"
                        )
                    },
                    label = {
                        Text(
                            text = "Card",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LiquidCyan,
                        selectedTextColor = LiquidCyan,
                        indicatorColor = Color(0xFF132030),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_card")
                )

                // Tab 2: Monthly Limits
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.MONTHLY_LIMITS,
                    onClick = { viewModel.setTab(ScreenTab.MONTHLY_LIMITS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.MONTHLY_LIMITS) Icons.Filled.Tune else Icons.Outlined.Tune,
                            contentDescription = "Limits"
                        )
                    },
                    label = {
                        Text(
                            text = "Limits",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldChampagne,
                        selectedTextColor = GoldChampagne,
                        indicatorColor = Color(0xFF261F12),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_limits")
                )

                // Tab 3: AI Coach
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.AI_COACH,
                    onClick = { viewModel.setTab(ScreenTab.AI_COACH) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.AI_COACH) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Coach"
                        )
                    },
                    label = {
                        Text(
                            text = "Advisor",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldChampagne,
                        selectedTextColor = GoldChampagne,
                        indicatorColor = Color(0xFF261F12),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_coach")
                )

                // Tab 4: Transactions Ledger
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.TRANSACTIONS,
                    onClick = { viewModel.setTab(ScreenTab.TRANSACTIONS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.TRANSACTIONS) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = "Ledger"
                        )
                    },
                    label = {
                        Text(
                            text = "Ledger",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LiquidCyan,
                        selectedTextColor = LiquidCyan,
                        indicatorColor = Color(0xFF132030),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_transactions")
                )
            }
        },
        floatingActionButton = {
            if (uiState.currentTab == ScreenTab.CARD_WALLET || uiState.currentTab == ScreenTab.TRANSACTIONS) {
                FloatingActionButton(
                    onClick = { showQuickPayModal = true },
                    containerColor = LiquidCyan,
                    contentColor = DarkBg,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("fab_quick_pay")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Pay via Paytm",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                ScreenTab.CARD_WALLET -> {
                    CardWalletScreen(
                        balance = uiState.paytmLinkedBalance,
                        isPaytmLinked = uiState.isPaytmLinked,
                        linkedPhone = uiState.linkedPhoneNumber,
                        isFrozen = uiState.isCardFrozen,
                        showDetails = uiState.showCardDetails,
                        isSyncing = uiState.isSyncingPaytm,
                        syncStatusText = uiState.lastSyncTime,
                        budget = currentBudget,
                        transactions = transactions,
                        latestAdviceItem = uiState.latestSpentItemWithAdvice,
                        onToggleFreeze = { viewModel.toggleCardFreeze() },
                        onToggleDetails = { viewModel.toggleCardDetails() },
                        onSyncClick = { viewModel.syncWithPaytm() },
                        onOpenPaymentModal = { showQuickPayModal = true },
                        onOpenLimitModal = { showLimitModal = true },
                        onOpenLinkModal = { showLinkPaytmModal = true },
                        onDismissAdvice = { viewModel.dismissLatestAdvice() },
                        onNavigateToCoach = { viewModel.setTab(ScreenTab.AI_COACH) },
                        onNavigateToTransactions = { viewModel.setTab(ScreenTab.TRANSACTIONS) }
                    )
                }

                ScreenTab.MONTHLY_LIMITS -> {
                    MonthlyLimitsScreen(
                        budget = currentBudget,
                        transactions = transactions,
                        onOpenEditLimitModal = { showLimitModal = true }
                    )
                }

                ScreenTab.AI_COACH -> {
                    AICoachScreen(
                        totalLimit = currentBudget.totalLimit,
                        totalSpent = transactions.sumOf { it.amount },
                        recommendations = viewModel.getSavingsRecommendations(),
                        conversation = uiState.coachConversation,
                        isAsking = uiState.isAskingCoach,
                        onAskQuestion = { q -> viewModel.askCoach(q) }
                    )
                }

                ScreenTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        transactions = transactions,
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                        onClearAll = { viewModel.clearAllTransactions() }
                    )
                }
            }
        }
    }

    // Link Paytm Modal
    if (showLinkPaytmModal) {
        LinkPaytmModal(
            sheetState = linkSheetState,
            accountDetails = paytmAccount,
            onDismiss = {
                coroutineScope.launch {
                    linkSheetState.hide()
                    showLinkPaytmModal = false
                }
            },
            onLinkConfirmed = { phone, upi, holder, balance ->
                viewModel.linkPaytmAccount(phone, upi, holder, balance)
                coroutineScope.launch {
                    linkSheetState.hide()
                    showLinkPaytmModal = false
                }
            },
            onUnlinkConfirmed = {
                viewModel.unlinkPaytmAccount()
                coroutineScope.launch {
                    linkSheetState.hide()
                    showLinkPaytmModal = false
                }
            },
            onSimulateSmsSync = { sms ->
                viewModel.syncFromSms(sms)
                coroutineScope.launch {
                    linkSheetState.hide()
                    showLinkPaytmModal = false
                }
            }
        )
    }

    // Quick Pay Modal
    if (showQuickPayModal) {
        QuickPayModal(
            sheetState = paySheetState,
            isProcessing = uiState.isPaymentInProgress,
            onDismiss = {
                coroutineScope.launch {
                    paySheetState.hide()
                    showQuickPayModal = false
                }
            },
            onPayConfirmed = { merchant, amount, category, note ->
                viewModel.makePaytmPayment(merchant, amount, category, note)
                coroutineScope.launch {
                    paySheetState.hide()
                    showQuickPayModal = false
                }
            }
        )
    }

    // Set Monthly Limit Modal
    if (showLimitModal) {
        LimitSettingsModal(
            sheetState = limitSheetState,
            currentBudget = currentBudget,
            onDismiss = {
                coroutineScope.launch {
                    limitSheetState.hide()
                    showLimitModal = false
                }
            },
            onSaveLimit = { newLimit ->
                viewModel.updateMonthlyLimit(newLimit)
                coroutineScope.launch {
                    limitSheetState.hide()
                    showLimitModal = false
                }
            }
        )
    }
}
