package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiAdvisorService
import com.example.data.local.AppDatabase
import com.example.data.local.MonthlyBudgetEntity
import com.example.data.local.PaytmAccountDetails
import com.example.data.local.PaytmAccountManager
import com.example.data.local.TransactionEntity
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentCategory
import com.example.data.model.SavingRecommendation
import com.example.data.model.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.regex.Pattern

class PayPulseRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val geminiService = GeminiAdvisorService()
    val accountManager = PaytmAccountManager(context)

    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactions().map { entities ->
        entities.map { it.toModel() }
    }

    val paytmAccount: StateFlow<PaytmAccountDetails> = accountManager.accountDetails

    fun getBudgetForMonth(monthKey: String): Flow<MonthlyBudget?> =
        budgetDao.getBudgetForMonth(monthKey).map { entity ->
            entity?.let {
                MonthlyBudget(
                    monthKey = it.monthKey,
                    displayMonth = it.displayMonth,
                    totalLimit = it.totalLimit,
                    diningLimit = it.diningLimit,
                    shoppingLimit = it.shoppingLimit,
                    groceriesLimit = it.groceriesLimit,
                    travelLimit = it.travelLimit,
                    billsLimit = it.billsLimit,
                    entertainmentLimit = it.entertainmentLimit,
                    healthLimit = it.healthLimit
                )
            }
        }

    suspend fun saveBudget(budget: MonthlyBudget) = withContext(Dispatchers.IO) {
        budgetDao.upsertBudget(
            MonthlyBudgetEntity(
                monthKey = budget.monthKey,
                displayMonth = budget.displayMonth,
                totalLimit = budget.totalLimit,
                diningLimit = budget.diningLimit,
                shoppingLimit = budget.shoppingLimit,
                groceriesLimit = budget.groceriesLimit,
                travelLimit = budget.travelLimit,
                billsLimit = budget.billsLimit,
                entertainmentLimit = budget.entertainmentLimit,
                healthLimit = budget.healthLimit
            )
        )
    }

    suspend fun makePayment(
        merchant: String,
        amount: Double,
        category: PaymentCategory,
        note: String,
        totalLimit: Double,
        currentSpent: Double,
        remainingBalance: Double
    ): TransactionItem = withContext(Dispatchers.IO) {
        val paytmRefId = "PTM-" + UUID.randomUUID().toString().take(8).uppercase()
        val now = System.currentTimeMillis()

        // Generate AI Financial Coach advice
        val aiAdvice = geminiService.getSpendingAdvice(
            merchant = merchant,
            amount = amount,
            category = category,
            totalMonthlyLimit = totalLimit,
            currentMonthSpent = currentSpent,
            remainingBalance = remainingBalance
        )

        val entity = TransactionEntity(
            title = merchant,
            merchantName = merchant,
            category = category.name,
            amount = amount,
            timestamp = now,
            paytmRefId = paytmRefId,
            note = note,
            aiAdvice = aiAdvice
        )

        val id = transactionDao.insertTransaction(entity)
        
        // Update real account balance in persistent preferences
        val currentAccountBal = paytmAccount.value.balance
        if (currentAccountBal > 0) {
            accountManager.updateBalance(maxOf(0.0, currentAccountBal - amount))
        }

        entity.copy(id = id).toModel()
    }

    suspend fun parseAndRecordSms(
        smsText: String,
        totalLimit: Double,
        currentSpent: Double,
        remainingBalance: Double
    ): TransactionItem? = withContext(Dispatchers.IO) {
        // Regex to extract amount from typical Indian Paytm SMS alerts:
        // Examples: "Paid Rs. 450 to Starbucks via Paytm...", "Sent Rs 250 to Uber...", "Debited INR 1,200.00 for Amazon..."
        val amountPattern = Pattern.compile("(?i)(?:rs\\.?|inr|₹)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)")
        val matcher = amountPattern.matcher(smsText)
        val amount = if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        if (amount <= 0.0) return@withContext null

        // Try extracting merchant
        val merchantPattern = Pattern.compile("(?i)(?:to|at|for)\\s+([A-Za-z0-9\\s&'-]+?)(?:\\s+(?:using|via|on|ref|vpa|from|with|bal|upi)|\\.|$)")
        val merchantMatcher = merchantPattern.matcher(smsText)
        val extractedMerchant = if (merchantMatcher.find()) {
            merchantMatcher.group(1)?.trim() ?: "Paytm Merchant"
        } else {
            "Paytm Merchant"
        }

        val category = guessCategory(extractedMerchant)

        makePayment(
            merchant = extractedMerchant,
            amount = amount,
            category = category,
            note = "Auto-synced from Paytm Alert",
            totalLimit = totalLimit,
            currentSpent = currentSpent,
            remainingBalance = remainingBalance
        )
    }

    private fun guessCategory(merchant: String): PaymentCategory {
        val lower = merchant.lowercase()
        return when {
            lower.contains("starbucks") || lower.contains("zomato") || lower.contains("swiggy") ||
                    lower.contains("cafe") || lower.contains("restaurant") || lower.contains("burger") ||
                    lower.contains("pizza") || lower.contains("food") || lower.contains("dining") -> PaymentCategory.DINING

            lower.contains("uber") || lower.contains("ola") || lower.contains("metro") ||
                    lower.contains("fuel") || lower.contains("petrol") || lower.contains("irctc") ||
                    lower.contains("flight") || lower.contains("toll") -> PaymentCategory.TRAVEL

            lower.contains("instamart") || lower.contains("blinkit") || lower.contains("zepto") ||
                    lower.contains("grocery") || lower.contains("supermarket") || lower.contains("mart") -> PaymentCategory.GROCERIES

            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("myntra") ||
                    lower.contains("zara") || lower.contains("mall") || lower.contains("retail") -> PaymentCategory.SHOPPING

            lower.contains("electricity") || lower.contains("bill") || lower.contains("bescom") ||
                    lower.contains("broadband") || lower.contains("airtel") || lower.contains("jio") ||
                    lower.contains("water") || lower.contains("gas") -> PaymentCategory.BILLS

            lower.contains("netflix") || lower.contains("spotify") || lower.contains("movie") ||
                    lower.contains("bookmyshow") || lower.contains("cinema") -> PaymentCategory.ENTERTAINMENT

            lower.contains("pharmacy") || lower.contains("hospital") || lower.contains("apollo") ||
                    lower.contains("doctor") || lower.contains("med") -> PaymentCategory.HEALTH

            else -> PaymentCategory.OTHER
        }
    }

    suspend fun askAICoach(question: String, totalLimit: Double, currentSpent: Double): String {
        return geminiService.askFinancialCoach(question, totalLimit, currentSpent)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
    }

    suspend fun initializeCleanDatabase() = withContext(Dispatchers.IO) {
        // Clear all previous dummy transactions
        transactionDao.clearAll()

        // Set up clean default budget for current month if not already created
        val defaultBudget = MonthlyBudget(
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
        saveBudget(defaultBudget)
    }

    fun getSmartSavingsTips(totalLimit: Double, totalSpent: Double): List<SavingRecommendation> {
        val remaining = maxOf(0.0, totalLimit - totalSpent)
        return listOf(
            SavingRecommendation(
                id = "tip_1",
                title = "Paytm Fuel & Transit Cashback",
                potentialSavings = 450.0,
                insight = "Link your Paytm Transit Card or Fastag for automated toll & fuel cashback rebates up to 10%.",
                actionText = "Transit Cashback",
                category = PaymentCategory.TRAVEL
            ),
            SavingRecommendation(
                id = "tip_2",
                title = "Weekend Discretionary Cap",
                potentialSavings = 1800.0,
                insight = "Pacing your weekend dining with a ₹1,200 cap protects your monthly reserve buffer.",
                actionText = "Weekend Guardrail",
                category = PaymentCategory.DINING
            ),
            SavingRecommendation(
                id = "tip_3",
                title = "Auto-Sweep Surplus to Digital Gold",
                potentialSavings = 2500.0,
                insight = "Remaining buffer is ₹${"%,.0f".format(remaining)}. Automate a daily sweep into 24K Digital Gold.",
                actionText = "Setup Gold Sweep",
                category = PaymentCategory.OTHER
            ),
            SavingRecommendation(
                id = "tip_4",
                title = "Utility Biller Direct Rebates",
                potentialSavings = 320.0,
                insight = "Setting Paytm auto-pay for electricity and broadband earns scratch cards and prevents late charges.",
                actionText = "Auto-Pay Bills",
                category = PaymentCategory.BILLS
            )
        )
    }
}
