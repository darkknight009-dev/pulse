package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PaytmAccountDetails
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentCategory
import com.example.data.model.SavingRecommendation
import com.example.data.model.TransactionItem
import com.example.data.repository.PayPulseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    CARD_WALLET,
    MONTHLY_LIMITS,
    AI_COACH,
    TRANSACTIONS
}

data class PayPulseUiState(
    val currentTab: ScreenTab = ScreenTab.CARD_WALLET,
    val selectedMonthKey: String = "2026-10",
    val selectedMonthDisplay: String = "October 2026",
    val paytmLinkedBalance: Double = 0.0,
    val isPaytmLinked: Boolean = false,
    val linkedPhoneNumber: String = "",
    val linkedUpiId: String = "",
    val isCardFrozen: Boolean = false,
    val showCardDetails: Boolean = false,
    val isSyncingPaytm: Boolean = false,
    val lastSyncTime: String = "Ready to Link",
    val latestSpentItemWithAdvice: TransactionItem? = null,
    val isAskingCoach: Boolean = false,
    val coachConversation: List<Pair<String, String>> = emptyList(),
    val isPaymentInProgress: Boolean = false
)

class PayPulseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PayPulseRepository(application)

    private val _uiState = MutableStateFlow(PayPulseUiState())
    val uiState: StateFlow<PayPulseUiState> = _uiState.asStateFlow()

    val paytmAccount: StateFlow<PaytmAccountDetails> = repository.paytmAccount

    val transactions: StateFlow<List<TransactionItem>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBudget: StateFlow<MonthlyBudget> = repository.getBudgetForMonth("2026-10")
        .combine(MutableStateFlow("2026-10")) { budget, _ ->
            budget ?: MonthlyBudget(
                monthKey = "2026-10",
                displayMonth = "October 2026",
                totalLimit = 35000.0,
                diningLimit = 8750.0,
                shoppingLimit = 7000.0,
                groceriesLimit = 8750.0,
                travelLimit = 4200.0,
                billsLimit = 3500.0,
                entertainmentLimit = 1750.0,
                healthLimit = 1050.0
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MonthlyBudget(
                monthKey = "2026-10",
                displayMonth = "October 2026",
                totalLimit = 35000.0,
                diningLimit = 8750.0,
                shoppingLimit = 7000.0,
                groceriesLimit = 8750.0,
                travelLimit = 4200.0,
                billsLimit = 3500.0,
                entertainmentLimit = 1750.0,
                healthLimit = 1050.0
            )
        )

    init {
        viewModelScope.launch {
            // Ensure clean database without any dummy data
            repository.initializeCleanDatabase()

            // Observe real Paytm account state
            paytmAccount.collect { acc ->
                _uiState.value = _uiState.value.copy(
                    isPaytmLinked = acc.isLinked,
                    linkedPhoneNumber = acc.phoneNumber,
                    linkedUpiId = acc.upiId,
                    paytmLinkedBalance = if (acc.isLinked) acc.balance else 0.0,
                    lastSyncTime = if (acc.isLinked) "Synced • Just now" else "Not Linked"
                )
            }
        }
    }

    fun setTab(tab: ScreenTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun toggleCardFreeze() {
        _uiState.value = _uiState.value.copy(isCardFrozen = !_uiState.value.isCardFrozen)
    }

    fun toggleCardDetails() {
        _uiState.value = _uiState.value.copy(showCardDetails = !_uiState.value.showCardDetails)
    }

    fun syncWithPaytm() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingPaytm = true)
            delay(1000)
            _uiState.value = _uiState.value.copy(
                isSyncingPaytm = false,
                lastSyncTime = "Live Sync • Just now"
            )
        }
    }

    fun linkPaytmAccount(phone: String, upiId: String, holderName: String, balance: Double) {
        repository.accountManager.linkAccount(phone, upiId, holderName, balance)
    }

    fun unlinkPaytmAccount() {
        repository.accountManager.unlinkAccount()
    }

    fun makePaytmPayment(
        merchant: String,
        amount: Double,
        category: PaymentCategory,
        note: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPaymentInProgress = true)

            val currentLimit = currentBudget.value.totalLimit
            val currentSpent = transactions.value.sumOf { it.amount }
            val remainingBal = _uiState.value.paytmLinkedBalance

            val newTx = repository.makePayment(
                merchant = merchant,
                amount = amount,
                category = category,
                note = note,
                totalLimit = currentLimit,
                currentSpent = currentSpent,
                remainingBalance = remainingBal
            )

            val updatedBalance = maxOf(0.0, _uiState.value.paytmLinkedBalance - amount)
            repository.accountManager.updateBalance(updatedBalance)

            _uiState.value = _uiState.value.copy(
                paytmLinkedBalance = updatedBalance,
                isPaymentInProgress = false,
                latestSpentItemWithAdvice = newTx
            )
        }
    }

    fun syncFromSms(smsText: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPaymentInProgress = true)
            val currentLimit = currentBudget.value.totalLimit
            val currentSpent = transactions.value.sumOf { it.amount }
            val remainingBal = _uiState.value.paytmLinkedBalance

            val tx = repository.parseAndRecordSms(
                smsText = smsText,
                totalLimit = currentLimit,
                currentSpent = currentSpent,
                remainingBalance = remainingBal
            )

            if (tx != null) {
                val updatedBalance = maxOf(0.0, _uiState.value.paytmLinkedBalance - tx.amount)
                repository.accountManager.updateBalance(updatedBalance)
                _uiState.value = _uiState.value.copy(
                    paytmLinkedBalance = updatedBalance,
                    latestSpentItemWithAdvice = tx
                )
            }
            _uiState.value = _uiState.value.copy(isPaymentInProgress = false)
        }
    }

    fun dismissLatestAdvice() {
        _uiState.value = _uiState.value.copy(latestSpentItemWithAdvice = null)
    }

    fun updateMonthlyLimit(newTotalLimit: Double) {
        viewModelScope.launch {
            val budget = currentBudget.value.copy(
                totalLimit = newTotalLimit,
                diningLimit = newTotalLimit * 0.25,
                shoppingLimit = newTotalLimit * 0.20,
                groceriesLimit = newTotalLimit * 0.25,
                travelLimit = newTotalLimit * 0.12,
                billsLimit = newTotalLimit * 0.10,
                entertainmentLimit = newTotalLimit * 0.05,
                healthLimit = newTotalLimit * 0.03
            )
            repository.saveBudget(budget)
        }
    }

    fun askCoach(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAskingCoach = true)
            val currentSpent = transactions.value.sumOf { it.amount }
            val answer = repository.askAICoach(
                question = question,
                totalLimit = currentBudget.value.totalLimit,
                currentSpent = currentSpent
            )
            val updatedList = _uiState.value.coachConversation + (question to answer)
            _uiState.value = _uiState.value.copy(
                isAskingCoach = false,
                coachConversation = updatedList
            )
        }
    }

    fun getSavingsRecommendations(): List<SavingRecommendation> {
        val totalSpent = transactions.value.sumOf { it.amount }
        return repository.getSmartSavingsTips(currentBudget.value.totalLimit, totalSpent)
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
        }
    }
}
