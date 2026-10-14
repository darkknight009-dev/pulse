package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AurumEnd
import com.example.ui.theme.AurumMid
import com.example.ui.theme.AurumStart
import com.example.ui.theme.ChipGold
import com.example.ui.theme.ChipGoldDark
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldCash
import com.example.ui.theme.GoldChampagne
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.ObsidianEnd
import com.example.ui.theme.ObsidianMid
import com.example.ui.theme.ObsidianStart
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PaytmBlue
import com.example.ui.theme.PaytmCyan
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VelvetEnd
import com.example.ui.theme.VelvetMid
import com.example.ui.theme.VelvetStart

enum class CardEdition(val title: String, val gradient: List<Color>, val accentColor: Color) {
    TITANIUM("Titanium Black", listOf(ObsidianStart, ObsidianMid, ObsidianEnd), GoldChampagne),
    VELVET("Midnight Velvet", listOf(VelvetStart, VelvetMid, VelvetEnd), LiquidCyan),
    AURUM("Imperial Aurum", listOf(AurumStart, AurumMid, AurumEnd), GoldLight)
}

@Composable
fun PaytmCardView(
    balance: Double,
    isPaytmLinked: Boolean,
    linkedPhone: String,
    isFrozen: Boolean,
    showDetails: Boolean,
    isSyncing: Boolean,
    syncStatusText: String,
    onToggleFreeze: () -> Unit,
    onToggleDetails: () -> Unit,
    onSyncClick: () -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember { mutableStateOf(false) }
    var selectedEdition by remember { mutableStateOf(CardEdition.TITANIUM) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Linked Account Status bar & Live Sync
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, if (isPaytmLinked) EmeraldCash.copy(alpha = 0.4f) else DarkBorderSubtle, RoundedCornerShape(24.dp))
                    .clickable { onLinkClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("link_paytm_status_pill")
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isFrozen) RoseCoral else if (isPaytmLinked) EmeraldCash else LiquidCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFrozen) "Card Frozen" else if (isPaytmLinked) "Paytm: +91 $linkedPhone" else "Link Real Paytm +",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isPaytmLinked) TextPrimary else LiquidCyan
                )
                if (isPaytmLinked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = EmeraldCash,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Sync with Paytm trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(24.dp))
                    .clickable { onSyncClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("sync_paytm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync",
                    tint = if (isSyncing) LiquidCyan else TextSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(if (isSyncing) spinAngle else 0f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSyncing) "Handshake..." else syncStatusText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = if (isSyncing) LiquidCyan else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ambient Aura Glow behind the card
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(180.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isFrozen) RoseCoral else selectedEdition.accentColor).copy(alpha = pulseGlow * 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 3D Flippable Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 18f * density
                    }
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = selectedEdition.accentColor,
                        spotColor = selectedEdition.accentColor
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                (if (isFrozen) RoseCoral else selectedEdition.accentColor).copy(alpha = 0.8f),
                                Color(0xFF202534),
                                (if (isFrozen) RoseCoral else selectedEdition.accentColor).copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { isFlipped = !isFlipped }
                    .testTag("paytm_card_view")
            ) {
                if (rotation <= 90f) {
                    // Front Face
                    CardFrontFace(
                        edition = selectedEdition,
                        balance = balance,
                        isFrozen = isFrozen,
                        showDetails = showDetails
                    )
                } else {
                    // Back Face (Mirrored so text reads correctly)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        CardBackFace(
                            edition = selectedEdition,
                            showDetails = showDetails
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Edition Switcher Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardEdition.values().forEach { edition ->
                val isSelected = selectedEdition == edition
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DarkSurfaceCard else DarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) edition.accentColor else DarkBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedEdition = edition }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(edition.accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = edition.title.split(" ").first(),
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Controls Row (Freeze, Flip, Details)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flip button
            CardActionPill(
                icon = Icons.Default.Flip,
                label = if (isFlipped) "View Front" else "Flip Card",
                tint = selectedEdition.accentColor,
                onClick = { isFlipped = !isFlipped },
                modifier = Modifier.weight(1f)
            )

            // Freeze button
            CardActionPill(
                icon = if (isFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                label = if (isFrozen) "Unfreeze" else "Freeze",
                tint = if (isFrozen) RoseCoral else TextSecondary,
                onClick = onToggleFreeze,
                modifier = Modifier.weight(1f)
            )

            // Details reveal button
            CardActionPill(
                icon = if (showDetails) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                label = if (showDetails) "Mask CVV" else "Show CVV",
                tint = if (showDetails) LiquidCyan else TextSecondary,
                onClick = onToggleDetails,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CardFrontFace(
    edition: CardEdition,
    balance: Double,
    isFrozen: Boolean,
    showDetails: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = edition.gradient))
    ) {
        // Luxury metal texture overlay
        Image(
            painter = painterResource(id = R.drawable.luxury_card_metal),
            contentDescription = "Texture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.35f)
        )

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Brand & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PAYTM",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp,
                            color = edition.accentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BLACK",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            color = TextPrimary.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = "PREMIER EDITION",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = edition.accentColor.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Contactless",
                        tint = edition.accentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(90f)
                    )
                }
            }

            // Middle: Smart Chip & Available Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Metal Chip
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(listOf(ChipGold, ChipGoldDark, ChipGold))
                        )
                        .border(1.dp, Color(0xFF6E4F00), RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center)
                            .border(1.dp, Color(0xFF4A3500), CircleShape)
                    )
                }

                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AVAILABLE BALANCE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${"%,.2f".format(balance)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = if (isFrozen) TextSecondary else edition.accentColor
                    )
                }
            }

            // Bottom: Number & Cardholder
            Column {
                Text(
                    text = if (showDetails) "4532  8910  6543  8821" else "4532  ••••  ••••  8821",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 2.5.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 8.sp,
                            letterSpacing = 1.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "ISHAAN RAI",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "VALID THRU",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "10/29",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        // RuPay Platinum Hologram
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, edition.accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RuPay",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = edition.accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardBackFace(edition: CardEdition, showDetails: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = edition.gradient))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Magnetic stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF060709))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Holographic Signature Panel & CVV
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signature strip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Ishaan Rai",
                        fontFamily = CinzelFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // CVV Box
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CVV",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF131722))
                            .border(1.dp, edition.accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showDetails) "742" else "•••",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = edition.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Regulatory Disclaimer & Concierge Helpline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Paytm Payments Bank Ltd • 24/7 Concierge: 1800-120-130",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 8.sp,
                    color = TextMuted
                )
                Text(
                    text = "This card is property of Paytm Payments Bank Ltd. Tap to flip.",
                    fontFamily = OutfitFontFamily,
                    fontSize = 8.sp,
                    color = TextMuted.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CardActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextPrimary
        )
    }
}
