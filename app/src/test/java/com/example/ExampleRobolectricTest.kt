package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PaymentCategory
import com.example.data.model.TransactionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PayPulse", appName)
    }

    @Test
    fun `transaction item model calculation`() {
        val tx = TransactionItem(
            id = 1,
            title = "Starbucks",
            merchantName = "Starbucks CyberHub",
            category = PaymentCategory.DINING,
            amount = 350.0,
            timestamp = System.currentTimeMillis(),
            paytmRefId = "PTM-123456"
        )
        assertEquals(350.0, tx.amount, 0.01)
        assertEquals(PaymentCategory.DINING, tx.category)
        assertTrue(tx.paytmRefId.startsWith("PTM-"))
    }
}
