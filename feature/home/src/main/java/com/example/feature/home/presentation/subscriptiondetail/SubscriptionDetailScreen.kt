package com.example.feature.home.presentation.subscriptiondetail

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Subscription
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.min

@Composable
fun SubscriptionDetailScreen(
    uiState: SubscriptionDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {
        when (uiState) {
            is SubscriptionDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.accent)
                )
            }
            is SubscriptionDetailUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is SubscriptionDetailUiState.Content -> {
                SubscriptionDetailContent(
                    subscription = uiState.subscription,
                    library = uiState.library,
                    qrBitmap = uiState.qrBitmap,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun SubscriptionDetailContent(
    subscription: Subscription,
    library: LibraryInfo?,
    qrBitmap: Bitmap?,
    onBackClick: () -> Unit
) {
    val libraryName = library?.name ?: subscription.libraryName ?: "Library"
    val membershipName = subscription.membershipName.ifBlank { "Membership" }
    val status = subscription.statusLabel()
    val statusColor = subscription.statusColor()
    val memberName = "Library Member"
    val memberTier = membershipName.ifBlank { "Member Tier" }.uppercase(Locale.US)
    val memberCodeSource = subscription.membershipId ?: subscription.id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.surface_light))
                    .clickable { onBackClick() }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.primary)
                )
            }
            Text(
                text = "Membership",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        LibraryPickerPill(
            libraryName = libraryName,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        MembershipCard(
            memberName = memberName,
            memberTier = memberTier,
            status = status,
            statusColor = statusColor,
            qrBitmap = qrBitmap,
            memberCode = formatMemberCode(memberCodeSource),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LibraryPickerPill(
    libraryName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colorResource(id = R.color.surface_light))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.border),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = libraryName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.primary),
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = colorResource(id = R.color.secondary)
            )
        }
    }
}

@Composable
private fun MembershipCard(
    memberName: String,
    memberTier: String,
    status: String,
    statusColor: Color,
    qrBitmap: Bitmap?,
    memberCode: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(colorResource(id = R.color.surface_light))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MemberAvatar()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memberName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.primary),
                    maxLines = 1
                )
                Text(
                    text = memberTier,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.secondary),
                    letterSpacing = 1.sp
                )
            }
            StatusPill(
                label = status.uppercase(Locale.US),
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            QrCodeCard(
                qrBitmap = qrBitmap,
                cardBackground = Color.White,
                qrSize = 220.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MEMBER CODE",
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.secondary),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = memberCode,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B82F6),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun MemberAvatar() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .border(2.dp, Color(0xFF3B82F6), CircleShape)
            .background(colorResource(id = R.color.border)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = colorResource(id = R.color.primary),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun QrCodeCard(
    qrBitmap: Bitmap?,
    cardBackground: Color,
    qrSize: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBackground)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.border),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        if (qrBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Subscription QR",
                modifier = Modifier.size(qrSize)
            )
        } else {
            CircularProgressIndicator(
                color = colorResource(id = R.color.accent),
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.surface_light))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = message,
            color = colorResource(id = R.color.secondary),
            fontSize = 13.sp
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onRetryClick() }
                .background(colorResource(id = R.color.background))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Retry",
                tint = colorResource(id = R.color.primary)
            )
        }
    }
}

private fun Subscription.statusLabel(): String {
    val now = java.util.Date()
    val expiry = parseDate(expiresAt) ?: return "Active"
    if (expiry.before(now)) return "Expired"

    val millisLeft = expiry.time - now.time
    val daysLeft = millisLeft / (1000L * 60L * 60L * 24L)
    return if (daysLeft <= 30) "Expiring" else "Active"
}

private fun Subscription.statusColor(): Color {
    return when (statusLabel().lowercase(Locale.US)) {
        "expired" -> Color(0xFFE53935)
        "expiring" -> Color(0xFFF57C00)
        else -> Color(0xFF3B82F6)
    }
}

private fun formatMemberCode(value: String): String {
    val cleaned = value.filter { it.isLetterOrDigit() }
    if (cleaned.isBlank()) return "--"
    val display = cleaned.takeLast(min(12, cleaned.length)).uppercase(Locale.US)
    return display.chunked(3).joinToString(" ")
}

private fun parseDate(value: String?): java.util.Date? {
    if (value.isNullOrBlank()) return null

    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return runCatching { utcParser.parse(value) }.getOrNull()
        ?: runCatching { dateOnlyParser.parse(value) }.getOrNull()
}
