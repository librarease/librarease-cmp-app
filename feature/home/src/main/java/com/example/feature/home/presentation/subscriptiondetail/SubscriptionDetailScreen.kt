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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.core.R
import com.example.feature.home.domain.model.LibraryInfo
import com.example.feature.home.domain.model.Subscription
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    onBackClick: () -> Unit
) {
    val libraryName = library?.name ?: subscription.libraryName ?: "Library"
    val membershipName = subscription.membershipName.ifBlank { "Membership" }
    val logoUrl = library?.logo ?: subscription.libraryLogoUrl
    val status = subscription.statusLabel()
    val statusColor = subscription.statusColor()

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.border),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.surface_light))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LibraryLogo(logoUrl = logoUrl)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = libraryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = membershipName,
                        fontSize = 13.sp,
                        color = colorResource(id = R.color.secondary)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QrCodeCard(subscriptionId = subscription.id)
            }

            DividerLine()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(
                    label = "Type",
                    value = membershipName,
                    icon = Icons.Outlined.AccountBalance
                )
                DetailRow(
                    label = "Status",
                    value = status,
                    icon = if (status.lowercase(Locale.US) == "active") {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.Warning
                    },
                    valueColor = statusColor,
                    badge = true
                )
                DetailRow(
                    label = "Valid Until",
                    value = formatFullDate(subscription.expiresAt),
                    icon = Icons.Outlined.DateRange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Show this QR code at the library entrance for quick check-in.",
            fontSize = 12.sp,
            color = colorResource(id = R.color.secondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LibraryLogo(logoUrl: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(id = R.color.border)),
        contentAlignment = Alignment.Center
    ) {
        if (!logoUrl.isNullOrBlank()) {
            val request = ImageRequest.Builder(context)
                .data(logoUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            SubcomposeAsyncImage(
                model = request,
                contentDescription = "Library logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.accent),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                },
                error = {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = colorResource(id = R.color.tertiary)
                    )
                }
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint = colorResource(id = R.color.tertiary)
            )
        }
    }
}

@Composable
private fun QrCodeCard(subscriptionId: String) {
    val qrBitmap by produceState<Bitmap?>(initialValue = null, key1 = subscriptionId) {
        value = withContext(Dispatchers.Default) {
            generateQrBitmap(subscriptionId, 420)
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colorResource(id = R.color.background))
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
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "Subscription QR",
                modifier = Modifier.size(190.dp)
            )
        } else {
            CircularProgressIndicator(
                color = colorResource(id = R.color.accent),
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = subscriptionId,
        fontSize = 12.sp,
        color = colorResource(id = R.color.secondary)
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = colorResource(id = R.color.primary),
    badge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(id = R.color.tertiary),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = colorResource(id = R.color.secondary)
            )
        }

        if (badge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(valueColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorResource(id = R.color.border))
    )
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

private fun generateQrBitmap(data: String, size: Int): Bitmap? {
    if (data.isBlank()) return null
    return try {
        val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
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
        else -> Color(0xFF2E7D32)
    }
}

private fun formatFullDate(value: String?): String {
    val date = parseDate(value) ?: return "--"
    val output = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    return output.format(date)
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
