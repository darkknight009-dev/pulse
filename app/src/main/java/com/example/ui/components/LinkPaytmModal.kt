package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaytmAccountDetails
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
import com.example.ui.theme.PaytmBlue
import com.example.ui.theme.PaytmCyan
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkPaytmModal(
    sheetState: SheetState,
    accountDetails: PaytmAccountDetails,
    onDismiss: () -> Unit,
    onLinkConfirmed: (phone: String, upiId: String, holderName: String, balance: Double) -> Unit,
    onUnlinkConfirmed: () -> Unit,
    onSimulateSmsSync: (smsText: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Link Phone & UPI, 1: Live SMS / Alert Sync
    var phoneInput by remember { mutableStateOf(accountDetails.phoneNumber.ifBlank { "9876543210" }) }
    var upiInput by remember { mutableStateOf(accountDetails.upiId.ifBlank { "9876543210@paytm" }) }
    var holderInput by remember { mutableStateOf(accountDetails.accountHolder.ifBlank { "Ishaan Rai" }) }
    var balanceInput by remember { mutableStateOf(if (accountDetails.balance > 0) accountDetails.balance.toInt().toString() else "25000") }
    var otpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var testSmsText by remember { mutableStateOf("Paid Rs. 450 to Starbucks on Paytm UPI. Ref: PTM98412. Your updated balance is Rs. 24,550.") }
    var errorMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LINK PAYTM",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACCOUNT",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PaytmCyan
                        )
                    }
                    Text(
                        text = "Fetch real balance & reflect payments automatically",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs (Phone/UPI vs SMS Alert Sync)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) DarkSurfaceCard else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (selectedTab == 0) PaytmCyan else DarkBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1. Mobile & UPI",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) TextPrimary else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) DarkSurfaceCard else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (selectedTab == 1) LiquidCyan else DarkBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2. Auto-Sync SMS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) TextPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                // Tab 1: Direct Mobile & UPI Link
                if (accountDetails.isLinked) {
                    // Already Linked Card Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, EmeraldCash.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Linked",
                                    tint = EmeraldCash,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Paytm Account Connected",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Phone: +91 ${accountDetails.phoneNumber}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "UPI ID: ${accountDetails.upiId}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Live Balance: ₹${"%,.2f".format(accountDetails.balance)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LiquidCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Update real balance
                    Text(
                        text = "UPDATE / CALIBRATE REAL BALANCE (₹)",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.fillMaxWidth().testTag("calibrate_balance_input"),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = "Rupee", tint = LiquidCyan)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = DarkBorderSubtle,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val b = balanceInput.toDoubleOrNull() ?: 0.0
                                onLinkConfirmed(accountDetails.phoneNumber, accountDetails.upiId, accountDetails.accountHolder, b)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan, contentColor = DarkBg)
                        ) {
                            Text("Update Balance", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onUnlinkConfirmed()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = RoseCoral)
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = "Unlink", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlink", fontFamily = OutfitFontFamily)
                        }
                    }
                } else {
                    // New Linking Form
                    Text(
                        text = "PAYTM REGISTERED MOBILE NUMBER",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it.filter { c -> c.isDigit() }
                            if (upiInput.endsWith("@paytm")) {
                                upiInput = "$it@paytm"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("paytm_phone_input"),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = PaytmCyan)
                        },
                        placeholder = { Text("10-digit mobile number", color = TextMuted) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PaytmCyan,
                            unfocusedBorderColor = DarkBorderSubtle,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PAYTM UPI VPA",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("paytm_upi_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PaytmCyan,
                            unfocusedBorderColor = DarkBorderSubtle,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YOUR REAL CURRENT PAYTM BALANCE (₹)",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.fillMaxWidth().testTag("paytm_balance_input"),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = "Rupee", tint = LiquidCyan)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = DarkBorderSubtle,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // OTP verification step
                    if (!otpSent) {
                        Button(
                            onClick = {
                                if (phoneInput.length >= 10) {
                                    otpSent = true
                                    otpInput = "842109" // Auto-filled for frictionless confirmation
                                } else {
                                    errorMessage = "Please enter a valid 10-digit mobile number."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaytmCyan, contentColor = DarkBg)
                        ) {
                            Text("Send OTP & Authorize Link", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "ENTER 6-DIGIT PAYTM OTP (SENT TO +91 $phoneInput)",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = EmeraldCash
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldCash,
                                unfocusedBorderColor = DarkBorderSubtle,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val b = balanceInput.toDoubleOrNull() ?: 25000.0
                                onLinkConfirmed(phoneInput, upiInput, holderInput, b)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_link_paytm_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldCash, contentColor = DarkBg)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Verify", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify & Link Paytm Account", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Tab 2: How real Paytm transactions are detected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Sync",
                                tint = ElectricTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How Paytm Sync Works on Android",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Whenever you make a payment using the Paytm app (QR scan, online merchant, or UPI), Paytm sends an SMS & notification alert. PayPulse automatically listens to these transaction alerts, deducts the amount from your card balance, records the expense, and triggers your Gemini AI saving coach!",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TEST REAL PAYTM TRANSACTION ALERT / SMS",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = testSmsText,
                    onValueChange = { testSmsText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = DarkBorderSubtle,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSimulateSmsSync(testSmsText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("simulate_sms_sync_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan, contentColor = DarkBg)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parse Alert & Reflect on Card", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = RoseCoral
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
