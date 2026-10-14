package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.PaymentCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAdvisorService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getSpendingAdvice(
        merchant: String,
        amount: Double,
        category: PaymentCategory,
        totalMonthlyLimit: Double,
        currentMonthSpent: Double,
        remainingBalance: Double
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // Check if key is available and not default placeholder
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateFallbackAdvice(
                merchant, amount, category, totalMonthlyLimit, currentMonthSpent, remainingBalance
            )
        }

        val prompt = """
            You are PayPulse AI, a hyper-smart, friendly, and practical personal finance coach built into a Paytm-linked payment card.
            The user just spent ₹${"%.2f".format(amount)} at "$merchant" in the category "${category.displayName}".
            Monthly Budget Status:
            - Monthly Limit: ₹${"%.2f".format(totalMonthlyLimit)}
            - Total Spent this month: ₹${"%.2f".format(currentMonthSpent + amount)}
            - Remaining Budget: ₹${"%.2f".format(remainingBalance - amount)}
            
            Provide 2-3 brief, punchy, high-impact financial coaching sentences:
            1. An immediate assessment of how this purchase impacts their monthly pace and category balance.
            2. One specific, realistic trick to save money or utilize their funds smarter over the next few days (e.g., Paytm cashback, meal prep, pause discretionary spend, high-yield liquid funds, 50/30/20 rule).
            Keep it encouraging, concise (under 50 words), and tailored to Indian fintech context (Paytm, UPI, Cashback).
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            val part = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(part)
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            } else {
                Log.w("GeminiAdvisor", "Gemini API failed code=${response.code}: $responseBody")
            }
        } catch (e: Exception) {
            Log.e("GeminiAdvisor", "Error calling Gemini API", e)
        }

        // Return fallback advice if API call fails
        generateFallbackAdvice(
            merchant, amount, category, totalMonthlyLimit, currentMonthSpent, remainingBalance
        )
    }

    suspend fun askFinancialCoach(
        question: String,
        totalLimit: Double,
        spentSoFar: Double
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "You've spent ₹${"%.0f".format(spentSoFar)} out of your ₹${"%.0f".format(totalLimit)} monthly limit. To save more, automate 20% into digital gold or recurring deposits right after salary day, and check your Paytm points for recharge vouchers!"
        }

        val prompt = """
            You are PayPulse AI Financial Coach. The user has a monthly limit of ₹${"%.0f".format(totalLimit)} and has spent ₹${"%.0f".format(spentSoFar)} so far.
            User Question: "$question"
            Answer concisely (2-4 clear sentences), actionable, encouraging, focusing on Indian personal finance, UPI habits, and budget discipline.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrBlank()) {
                val json = JSONObject(body)
                val text = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return@withContext text.trim()
            }
        } catch (e: Exception) {
            Log.e("GeminiAdvisor", "Ask error", e)
        }

        "To reach your monthly savings target, try setting a ₹500/day daily cap for discretionary spend over the next week. You can also redeem Paytm Cashback points towards utility bills!"
    }

    private fun generateFallbackAdvice(
        merchant: String,
        amount: Double,
        category: PaymentCategory,
        totalMonthlyLimit: Double,
        currentMonthSpent: Double,
        remainingBalance: Double
    ): String {
        val newSpent = currentMonthSpent + amount
        val pct = if (totalMonthlyLimit > 0) (newSpent / totalMonthlyLimit) * 100 else 0.0

        return when (category) {
            PaymentCategory.DINING -> {
                if (pct > 75) {
                    "₹${"%.0f".format(amount)} spent on Dining. You've reached ${"%.0f".format(pct)}% of your monthly limit! Try home cooking for the next 4 days to save ~₹1,800 and preserve your buffer."
                } else {
                    "₹${"%.0f".format(amount)} at $merchant. You're pacing comfortably at ${"%.0f".format(pct)}% of budget. Tip: Look out for Paytm UPI dining cashback vouchers to get 5-10% back!"
                }
            }
            PaymentCategory.SHOPPING -> {
                "₹${"%.0f".format(amount)} shopping purchase recorded. Tip: Apply the 48-hour rule for non-essential items above ₹1,000. Put saved cash into Paytm Money liquid fund for 6.8% returns."
            }
            PaymentCategory.GROCERIES -> {
                "₹${"%.0f".format(amount)} grocery expense logged. Essential spending is well-allocated. Tip: Bulk buy non-perishables and check instant store card discounts to shave 8-12% off monthly groceries."
            }
            PaymentCategory.TRAVEL -> {
                "₹${"%.0f".format(amount)} commute expense. If travel exceeds ₹3,500/mo, consider metro passes or pooling rides during peak hours to save up to ₹1,200."
            }
            PaymentCategory.BILLS -> {
                "₹${"%.0f".format(amount)} utility bill paid on time! Pay via Paytm auto-debit before due dates to avoid late fees and earn loyalty scratch cards."
            }
            PaymentCategory.ENTERTAINMENT -> {
                "₹${"%.0f".format(amount)} for entertainment. Keep discretionary fun capped at 10% of total budget so your wealth building stays on autopilot."
            }
            PaymentCategory.HEALTH -> {
                "₹${"%.0f".format(amount)} invested in health & wellness. Prioritizing health prevents high future medical expenses—great job keeping this categorized!"
            }
            PaymentCategory.OTHER -> {
                "₹${"%.0f".format(amount)} reflected on your card. With ₹${"%.0f".format(remainingBalance - amount)} remaining, keep daily spends below ₹${"%.0f".format(maxOf(100.0, (remainingBalance - amount) / 15))} to finish the month in the green."
            }
        }
    }
}
