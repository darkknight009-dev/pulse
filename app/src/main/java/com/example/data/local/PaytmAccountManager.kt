package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PaytmAccountDetails(
    val isLinked: Boolean = false,
    val phoneNumber: String = "",
    val upiId: String = "",
    val accountHolder: String = "",
    val balance: Double = 0.0,
    val lastSyncTimestamp: Long = 0L,
    val autoSyncSms: Boolean = true
)

class PaytmAccountManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("paytm_account_prefs", Context.MODE_PRIVATE)

    private val _accountDetails = MutableStateFlow(loadAccountDetails())
    val accountDetails: StateFlow<PaytmAccountDetails> = _accountDetails.asStateFlow()

    private fun loadAccountDetails(): PaytmAccountDetails {
        val isLinked = prefs.getBoolean("is_linked", false)
        val phone = prefs.getString("phone_number", "") ?: ""
        val upi = prefs.getString("upi_id", "") ?: ""
        val holder = prefs.getString("account_holder", "") ?: ""
        val balance = prefs.getFloat("balance", 0.0f).toDouble()
        val lastSync = prefs.getLong("last_sync", 0L)
        val autoSync = prefs.getBoolean("auto_sync_sms", true)

        return PaytmAccountDetails(
            isLinked = isLinked,
            phoneNumber = phone,
            upiId = upi,
            accountHolder = holder,
            balance = balance,
            lastSyncTimestamp = lastSync,
            autoSyncSms = autoSync
        )
    }

    fun linkAccount(
        phone: String,
        upiId: String,
        holderName: String,
        initialBalance: Double
    ) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean("is_linked", true)
            .putString("phone_number", phone)
            .putString("upi_id", upiId)
            .putString("account_holder", holderName)
            .putFloat("balance", initialBalance.toFloat())
            .putLong("last_sync", now)
            .apply()

        _accountDetails.value = PaytmAccountDetails(
            isLinked = true,
            phoneNumber = phone,
            upiId = upiId,
            accountHolder = holderName,
            balance = initialBalance,
            lastSyncTimestamp = now,
            autoSyncSms = true
        )
    }

    fun updateBalance(newBalance: Double) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putFloat("balance", newBalance.toFloat())
            .putLong("last_sync", now)
            .apply()

        _accountDetails.value = _accountDetails.value.copy(
            balance = newBalance,
            lastSyncTimestamp = now
        )
    }

    fun unlinkAccount() {
        prefs.edit().clear().apply()
        _accountDetails.value = PaytmAccountDetails()
    }
}
